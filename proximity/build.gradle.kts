import project.convention.logic.version

plugins {
    alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.swiyu.kmp.maven.publish)
    alias(libs.plugins.swiyu.kmp.module.version)
}

moduleVersion {
    moduleVersion.set(version("proximity"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // Opt-in for expect/actual classes
        freeCompilerArgs.add("-XXLanguage:+WhenGuards") // Opt-in for when guards
    }
    // iOS targets
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            linkerOpts("-framework", "Security")
            // Export Kapun SDK libraries to iOS
            export(libs.kapun.proximity)
            export(libs.kapun.dcql) // Needed for DcqlQuery type
            export(libs.kapun.presentation)
            export(libs.kapun.credentials)
            export(libs.kapun.util)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Kapun SDK KMP libraries
            api(libs.kapun.proximity)
            api(libs.kapun.dcql) // For DcqlQuery type
            api(libs.kapun.presentation)
            api(libs.kapun.credentials)
            api(libs.kapun.util) // For Logger and Value
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

