import dev.detekt.gradle.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import project.convention.logic.lib
import project.convention.logic.plugin

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(plugin("detekt"))
            }
            dependencies {
                add("detektPlugins", lib("detekt-ktlint-wrapper"))
            }

            tasks.register("detektAll", Detekt::class) {
                group = "verification"
                description = "Run detekt on whole project"

                buildUponDefaultConfig.set(true)
                config.setFrom("$rootDir/config/detekt/detekt.yml")
                autoCorrect.set(false)
                basePath.set(rootDir.absolutePath)
                exclude("**/build/**", "build-logic/**", "**/*Test/**", "**/*Test.kt", "**/*Spec.kt")
                include("**/src/**/*.kt", "**/src/**/*.kts")
                parallel.set(true)
                setSource(files(projectDir))

                reports {
                    html {
                        outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.html"))
                    }

                    markdown {
                        outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.md"))
                    }

                    checkstyle {
                        outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
                    }

                    sarif {
                        required.set(false)
                    }
                }
            }
        }
    }
}
