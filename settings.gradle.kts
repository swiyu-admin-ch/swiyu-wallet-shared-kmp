pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "swiyu-shared-kmp"
include(":library")
include(":dcql")
include(":proximity")
include(":consistency")
include(":password-strength-estimator")
