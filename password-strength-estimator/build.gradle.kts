import com.vanniktech.maven.publish.DeploymentValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.uniffi.plugin)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        namespace = "ch.admin.foitt.swiyu.shared.passwordstrengthestimator"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
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

uniffi {
    generateFromLibrary()
}

cargo {
    packageDirectory = layout.projectDirectory.dir("rust/zxcvbn")
    ndkVersion = libs.versions.android.ndk.get()
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
