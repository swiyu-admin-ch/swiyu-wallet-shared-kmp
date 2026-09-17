import com.vanniktech.maven.publish.DeploymentValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        namespace = "ch.admin.foitt.swiyu.shared.proximity"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // Opt-in for expect/actual classes
        freeCompilerArgs.add("-XXLanguage:+WhenGuards") // Opt-in for when guards
    }

    jvm() // enabled for tests

    // iOS targets
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            linkerOpts("-framework", "Security")
            // Export heidi libraries to iOS
            export(libs.heidi.proximity)
            export(libs.heidi.dcql) // Needed for DcqlQuery type
            export(libs.heidi.presentation)
            export(libs.heidi.credentials)
            export(libs.heidi.util)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Heidi KMP libraries
            api(libs.heidi.proximity)
            api(libs.heidi.dcql) // For DcqlQuery type
            api(libs.heidi.presentation)
            api(libs.heidi.credentials)
            api(libs.heidi.util) // For Logger and Value
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies { }

        iosMain.dependencies { }
    }
}



mavenPublishing {

    publishToMavenCentral(validateDeployment = DeploymentValidation.NONE)
    signAllPublications()

    coordinates(
        group.toString(),
        "proximity",
        version.toString()
    )

    pom {
        name = "proximity"
        inceptionYear = "2026"
        description = "proximity library used in the Swiyu Android wallet"
        url = "https://github.com/swiyu-admin-ch/swiyu-wallet-shared-kmp"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "swiyu-admin-ch"
                name = "swiyu - the Swiss Trust Infrastructure ecosystem"
                url = "https://github.com/swiyu-admin-ch"
            }
        }
        scm {
            url = "https://github.com/swiyu-admin-ch/swiyu-wallet-shared-kmp"
        }
    }
}
