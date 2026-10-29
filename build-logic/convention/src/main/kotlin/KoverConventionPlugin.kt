import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import project.convention.logic.plugin

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin("kover"))
            }

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            androidGeneratedClasses()
                            classes("*\$\$serializer") // kotlinx.serialization generated
                            packages("uniffi.*") // UniFFI generated bindings
                        }
                    }
                }
            }
        }
    }
}
