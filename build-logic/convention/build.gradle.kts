plugins {
    `kotlin-dsl`
}

group = "project.build.convention.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.mavenPublish.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = libs.plugins.swiyu.kmp.library.get().pluginId
            implementationClass = "KmpLibraryPlugin"
        }
        register("detektConventionPlugin") {
            id = libs.plugins.swiyu.kmp.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("koverConventionPlugin") {
            id = libs.plugins.swiyu.kmp.kover.get().pluginId
            implementationClass = "KoverConventionPlugin"
        }
        register("mavenPublishConventionPlugin") {
            id = libs.plugins.swiyu.kmp.maven.publish.get().pluginId
            implementationClass = "MavenPublishConventionPlugin"
        }
        register("moduleVersionConventionPlugin") {
            id = libs.plugins.swiyu.kmp.module.version.get().pluginId
            implementationClass = "ModuleVersionConventionPlugin"
        }
        register("jnaNativeFixConventionPlugin") {
            id = libs.plugins.swiyu.kmp.jna.native.fix.get().pluginId
            implementationClass = "JnaNativeFixConventionPlugin"
        }
    }
}
