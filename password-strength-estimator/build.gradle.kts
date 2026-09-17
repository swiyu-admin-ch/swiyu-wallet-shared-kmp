import com.vanniktech.maven.publish.DeploymentValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.uniffi.plugin)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidTarget()
    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.all {
            linkerOpts("-framework", "Security")
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "ch.admin.foitt.swiyu.shared.passwordstrengthestimator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    ndkVersion = libs.versions.android.ndk.get()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // TODO: enable android tests
    testOptions {
        unitTests.all { it.isEnabled = false }
    }
}

uniffi {
    generateFromLibrary()
}

cargo {
    packageDirectory = layout.projectDirectory.dir("rust/zxcvbn")
}



mavenPublishing {

    publishToMavenCentral(validateDeployment = DeploymentValidation.NONE)
    signAllPublications()

    coordinates(
        group.toString(),
        "password-strength-estimator",
        version.toString()
    )

    pom {
        name = "password-strength-estimator"
        inceptionYear = "2026"
        description = "password-strength-estimator library used in the Swiyu Android wallet"
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
