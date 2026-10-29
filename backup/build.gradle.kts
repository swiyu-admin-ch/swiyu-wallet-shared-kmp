import project.convention.logic.lib
import project.convention.logic.version

plugins {
    alias(libs.plugins.swiyu.kmp.module.version)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.swiyu.kmp.maven.publish)
}

moduleVersion {
    moduleVersion.set(version("backup"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes") // Opt-in for expect/actual classes
    }

    sourceSets {
        commonMain.dependencies {
            implementation(lib("kotlinx-serialization-json"))
            implementation(lib("kotlinx-io-core"))

            implementation(libs.kotlinx.datetime)
            implementation(libs.kapun.crypto)
        }
        iosMain.dependencies {
            implementation(libs.kzip)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.jose4j)
        }
    }
}
