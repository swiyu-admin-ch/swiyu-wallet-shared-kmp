import org.gradle.api.Plugin
import org.gradle.api.Project
import project.convention.logic.ModuleVersionExtension

/**
 * Each module gets its own version from gradle/libs.versions.toml under its module name:
 *
 * ```
 * # gradle/libs.versions.toml
 * <module> = "0.3.0"

 * # <module>/build.gradle.kts
 * moduleVersion {
 *     moduleVersion.set(version("<module>"))
 * }
 * ```
*/
class ModuleVersionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Register the extension under the name "moduleVersion"
        val ext = target.extensions.create(
            "moduleVersion",
            ModuleVersionExtension::class.java
        )

        // Wire it lazily: only read the value after the module has configured it
        target.afterEvaluate {
            target.version = ext.moduleVersion.get()
        }
    }
}