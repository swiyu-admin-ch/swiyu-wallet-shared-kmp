import com.vanniktech.maven.publish.DeploymentValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        namespace = "ch.admin.foitt.swiyu.shared.backup"
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}



mavenPublishing {

    publishToMavenCentral(validateDeployment = DeploymentValidation.NONE)
    if (providers.gradleProperty("useSigning").getOrElse("true").toBoolean()) {
        signAllPublications()
    }

    coordinates(
        group.toString(),
        "backup",
        version.toString()
    )

    pom {
        name = "backup"
        inceptionYear = "2026"
        description = "backup library used in the Swiyu Android wallet"
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

tasks.register<Exec>("publishBackupToMavenLocal") {
    group = "publishing"
    description = "Publishes backup module to mavenLocal with pinned release version and signing disabled"

    workingDir = rootProject.projectDir
    commandLine(
        "./gradlew",
        ":backup:publishAndroidReleasePublicationToMavenLocal",
        "-PreleaseVersion=0.2.0",
        "-PuseSigning=false"
    )
}