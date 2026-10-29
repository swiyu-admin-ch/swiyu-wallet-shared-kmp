import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import java.util.Collections
import java.util.zip.ZipFile
import javax.inject.Inject

/**
 * Extracts the darwin JNA native libraries shipped with jars
 * (e.g. `org.kapunsdk:kapun-*-jvm` -> `libkapun_crypto_rust.dylib`, `libkapun_util_rust.dylib`, ...)
 * from the resolved jvm runtime classpath into `build/jna-libs/` and, if needed, patches their
 * Mach-O LINKEDIT so they pass the stricter dyld validation introduced with macOS 26/27.
 *
 * Background: the darwin dylib libraries published in some libraries (e.g. Kapun SDK 1.0.0-RC15)
 * have LC_SYMTAB string pools that are not 8-byte aligned (odd indirect symbol table counts,
 * the linker that produced them did not pad). dyld on macOS 26/27 rejects such images with
 * "mis-aligned LINKEDIT string pool", which makes JNA-based tests fail with
 * `UnsatisfiedLinkError`.
 *
 * The patch inserts 4 padding bytes before the string pool and adjusts the affected offsets,
 * then re-signs the library ad-hoc. Libraries whose string pool is already aligned are copied
 * unchanged, so this task degrades to a plain copy once the libraries ships fixed dylibs.
 * On non-macOS hosts the task does nothing and JNA extracts the platform libraries from the
 * jars as before.
 *
 * Wire-up: `jvmTest` depends on this task and gets `-Djna.library.path` pointing at
 * `build/jna-libs`, so JNA prefers the (patched) libraries over its broken jar extraction.
 */
class JnaNativeFixConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val prepareJnaLibs = tasks.register("prepareJnaLibs", PrepareJnaLibsTask::class.java)

            // The jars arrive via the jvm runtime classpath. Wire it lazily because
            // the jvm target (and its configurations) may be created after this plugin is applied.
            configurations.matching { it.name == "jvmRuntimeClasspath" }.configureEach {
                val runtimeClasspath = this
                prepareJnaLibs.configure { runtimeJars.from(runtimeClasspath) }
            }

            tasks.withType(KotlinJvmTest::class.java).configureEach {
                dependsOn(prepareJnaLibs)
                if (isMacOs()) {
                    jvmArgs(
                        "-Djna.library.path=${layout.buildDirectory.dir("jna-libs").get().asFile.absolutePath}"
                    )
                }
            }
        }
    }
}

/**
 * Extracts and (if needed) patches all darwin dylibs matching the current host architecture
 * from every runtime classpath jar. Runs on every build (no up-to-date caching) since it is
 * cheap and the classpath may change between runs.
 */
open class PrepareJnaLibsTask @Inject constructor(
    private val execOps: ExecOperations,
) : DefaultTask() {

    /** Resolved runtime classpath jars containing the native libraries. */
    @get:Internal
    val runtimeJars: ConfigurableFileCollection = project.objects.fileCollection()

    @TaskAction
    fun prepare() {
        if (!isMacOs()) {
            logger.info("jna-native-fix: not macOS, skipping (JNA extracts the platform libraries from the jars)")
            return
        }

        val resourcePrefix = when (System.getProperty("os.arch")) {
            "aarch64" -> "darwin-aarch64"
            "x86_64", "amd64" -> "darwin-x86-64"
            else -> {
                logger.warn("jna-native-fix: unsupported host architecture, skipping")
                return
            }
        }

        val jars = runtimeJars
            .filter { file -> file.name.endsWith(".jar") }
            .filter { file -> EXCLUDED_JAR_SUFFIXES.none { file.name.contains(it) } }
        if (jars.isEmpty) {
            logger.warn("jna-native-fix: no runtime classpath jars found, skipping")
            return
        }

        val outDir = project.layout.buildDirectory.dir("jna-libs").get().asFile
        outDir.mkdirs()
        var extracted = 0
        var patched = 0
        val patchedLibs = mutableSetOf<String>()

        for (jar in jars) {
            ZipFile(jar).use { zip ->
                for (entry in Collections.list(zip.entries())) {
                    if (entry.isDirectory) continue
                    if (!entry.name.startsWith("$resourcePrefix/")) continue
                    if (!entry.name.endsWith(".dylib")) continue

                    val dylibName = entry.name.substringAfterLast('/')
                    val data = zip.getInputStream(entry).readBytes()
                    val out = outDir.resolve(dylibName)

                    if (readU32(data, 0) != MH_MAGIC_64) {
                        logger.warn("jna-native-fix: ${entry.name} is not a 64-bit Mach-O, copying unchanged")
                        out.writeBytes(data)
                        extracted++
                        continue
                    }

                    val layout = parseLinkedit(data)
                    if (layout == null) {
                        logger.warn("jna-native-fix: could not locate LC_SYMTAB/__LINKEDIT in ${entry.name}, copying unchanged")
                        out.writeBytes(data)
                        extracted++
                        continue
                    }
                    val (stroff, stroffPos, linkeditFilesize, linkeditFilesizePos) = layout

                    if (stroff % 8 == 0) {
                        out.writeBytes(data)
                        extracted++
                        continue
                    }

                    // Insert 4 padding bytes before the string pool so stroff becomes 8-byte
                    // aligned, then fix the offsets that are affected by the insertion.
                    val fixed = ByteArray(data.size + 4)
                    System.arraycopy(data, 0, fixed, 0, stroff)
                    System.arraycopy(data, stroff, fixed, stroff + 4, data.size - stroff)
                    writeU32(fixed, stroffPos, stroff + 4)
                    writeU64(fixed, linkeditFilesizePos, linkeditFilesize + 4)

                    out.parentFile.mkdirs()
                    out.writeBytes(fixed)

                    // The original (ad-hoc) signature is invalidated by the patch, so re-sign.
                    execOps.exec {
                        commandLine("codesign", "--remove-signature", out.absolutePath)
                        isIgnoreExitValue = true
                    }
                    execOps.exec {
                        commandLine("codesign", "-s", "-", "-f", out.absolutePath)
                    }

                    extracted++
                    patched++
                    patchedLibs += "$dylibName (${jar.name})"
                }
            }
        }

        if (patched > 0) {
            logger.warn(
                "jna-native-fix: patched mis-aligned LINKEDIT string pools in ${patchedLibs.sorted()}. " +
                    "This workaround can be removed once the library ships aligned darwin dylibs."
            )
        } else {
            logger.lifecycle("jna-native-fix: extracted $extracted darwin dylib(s), all aligned, nothing patched")
        }
    }

    /** Locates LC_SYMTAB.stroff and __LINKEDIT.filesize in a 64-bit Mach-O image. */
    private fun parseLinkedit(data: ByteArray): LinkeditLayout? {
        val ncmds = readU32(data, 16)
        var off = HEADER_SIZE
        var stroff = -1
        var stroffPos = -1
        var linkeditFilesize = -1L
        var linkeditFilesizePos = -1
        repeat(ncmds) {
            val cmd = readU32(data, off)
            val cmdsize = readU32(data, off + 4)
            when (cmd) {
                LC_SYMTAB -> {
                    stroff = readU32(data, off + 16)
                    stroffPos = off + 16
                }
                LC_SEGMENT_64 -> {
                    val segname = String(data, off + 8, 16).trimEnd('\u0000')
                    if (segname == "__LINKEDIT") {
                        linkeditFilesize = readU64(data, off + 48)
                        linkeditFilesizePos = off + 48
                    }
                }
            }
            off += cmdsize
        }
        if (stroff < 0 || linkeditFilesizePos < 0) return null
        return LinkeditLayout(stroff, stroffPos, linkeditFilesize, linkeditFilesizePos)
    }

    private data class LinkeditLayout(
        val stroff: Int,
        val stroffPos: Int,
        val linkeditFilesize: Long,
        val linkeditFilesizePos: Int,
    )
}

private const val MH_MAGIC_64: Int = 0xfeedfacf.toInt()
private const val LC_SYMTAB = 0x2
private const val LC_SEGMENT_64 = 0x19
private const val HEADER_SIZE = 32
private val EXCLUDED_JAR_SUFFIXES = listOf("-sources", "-metadata", "-javadoc")

private fun readU32(data: ByteArray, off: Int): Int =
    (data[off].toInt() and 0xFF) or
        ((data[off + 1].toInt() and 0xFF) shl 8) or
        ((data[off + 2].toInt() and 0xFF) shl 16) or
        ((data[off + 3].toInt() and 0xFF) shl 24)

private fun writeU32(data: ByteArray, off: Int, value: Int) {
    data[off] = (value and 0xFF).toByte()
    data[off + 1] = ((value shr 8) and 0xFF).toByte()
    data[off + 2] = ((value shr 16) and 0xFF).toByte()
    data[off + 3] = ((value shr 24) and 0xFF).toByte()
}

private fun readU64(data: ByteArray, off: Int): Long {
    var value = 0L
    for (i in 7 downTo 0) value = (value shl 8) or (data[off + i].toLong() and 0xFF)
    return value
}

private fun writeU64(data: ByteArray, off: Int, value: Long) {
    for (i in 0 until 8) data[off + i] = ((value shr (8 * i)) and 0xFF).toByte()
}

private fun isMacOs(): Boolean =
    System.getProperty("os.name")?.lowercase()?.contains("mac") == true
