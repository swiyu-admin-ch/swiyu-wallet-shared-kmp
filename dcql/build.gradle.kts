import com.vanniktech.maven.publish.DeploymentValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        namespace = "ch.admin.foitt.swiyu.shared.dcql"
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
        commonMain.dependencies {
            api(libs.heidi.credentials)
			api(libs.heidi.crypto)
            api(libs.heidi.dcql)
            api(libs.heidi.util)
            api(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}



mavenPublishing {

	publishToMavenCentral(validateDeployment = DeploymentValidation.NONE)
	signAllPublications()

	coordinates(
		group.toString(),
		"dcql",
		version.toString()
	)

	pom {
		name = "dcql"
		inceptionYear = "2026"
		description = "dcql library used in the Swiyu Android wallet"
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
