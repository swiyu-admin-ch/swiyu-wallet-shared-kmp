import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import project.convention.logic.defaultNamespace
import project.convention.logic.lib
import project.convention.logic.plugin
import project.convention.logic.version
import kotlin.apply

class KmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // Core KMP plugins
                apply(plugin("kotlinMultiplatform"))
                apply(plugin("android-kotlin-multiplatform-library"))

                // Convention plugins
                apply(plugin("swiyu-kmp-kover"))
            }

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(version("toolchain-jdk").toInt())

                jvm()

                // iOS targets
                listOf(
                    iosArm64(),
                    iosSimulatorArm64(),
                ).forEach { iosTarget ->
                    iosTarget.binaries.all {
                        linkerOpts("-framework", "Security")
                    }
                }

                sourceSets.apply {
                    commonMain.dependencies {}

                    commonTest.dependencies {
                        implementation(lib("kotlin-test"))
                    }

                    androidMain.dependencies { }

                    iosMain.dependencies { }
                }

                val androidExt =
                    extensions.getByName("android") as KotlinMultiplatformAndroidLibraryExtension
                with(androidExt) {
                    namespace = defaultNamespace()
                    compileSdk = version("android-compileSdk").toInt()
                    minSdk = version("android-minSdk").toInt()
                }
            }
        }
    }
}
