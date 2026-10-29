import project.convention.logic.version

plugins {
	alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.swiyu.kmp.maven.publish)
    alias(libs.plugins.swiyu.kmp.module.version)
    alias(libs.plugins.swiyu.kmp.jna.native.fix)
}

moduleVersion {
    moduleVersion.set(version("dcql"))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kapun.credentials)
			api(libs.kapun.crypto)
            api(libs.kapun.dcql)
            api(libs.kapun.util)
            api(libs.kotlinx.serialization.json)
        }
    }
}

