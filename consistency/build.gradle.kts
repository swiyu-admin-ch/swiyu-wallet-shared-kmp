import project.convention.logic.version

plugins {
    alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.swiyu.kmp.maven.publish)
    alias(libs.plugins.swiyu.kmp.module.version)
    alias(libs.plugins.swiyu.kmp.jna.native.fix)
}

moduleVersion {
    moduleVersion.set(version("consistency"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // Opt-in for expect/actual classes
        freeCompilerArgs.add("-XXLanguage:+WhenGuards") // Opt-in for when guards
    }

    sourceSets {
        commonMain.dependencies {
            // Kapun SDK KMP libraries
            implementation(libs.kapun.credentials)
            implementation(libs.kapun.crypto)
            implementation(libs.kapun.util) // For Logger and Value
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kapun.crypto)
        }
    }
}
