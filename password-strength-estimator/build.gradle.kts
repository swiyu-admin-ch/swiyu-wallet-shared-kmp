import project.convention.logic.version

plugins {
    alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.uniffi.plugin)
    alias(libs.plugins.swiyu.kmp.maven.publish)
    alias(libs.plugins.swiyu.kmp.module.version)
}

moduleVersion {
    moduleVersion.set(version("password-strength-estimator"))
}

uniffi {
    generateFromLibrary()
}

cargo {
    packageDirectory = layout.projectDirectory.dir("rust/zxcvbn")
    ndkVersion = libs.versions.android.ndk.get()
}
