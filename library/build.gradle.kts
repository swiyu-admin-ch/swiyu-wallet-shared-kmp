import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.SuppressSkieWarning
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.swiyu.kmp.library)
    alias(libs.plugins.skie)
}

kotlin {
    val bundleId = "ch.admin.foitt.swiyu.shared.kmp"
    val frameworkName = "BITSwiyuSharedKMP"
    val xcf = XCFramework(frameworkName)
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkName
            isStatic = true
            export(project(":dcql"))
            export(project(":proximity"))
            export(project(":consistency"))
            export(project(":backup"))
			export(libs.kapun.proximity)
			export(project(":password-strength-estimator"))

            // Ensure swiftinterface generation and stable bundle id.
            binaryOption("bundleId", bundleId)
            binaryOption("bundleShortVersionString", version.toString())
            freeCompilerArgs += listOf(
                "-Xbinary=bundleId=${bundleId}",
                "-Xbinary=bundleShortVersionString=${version}"
            )

            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":dcql"))
            api(project(":proximity"))
            api(project(":consistency"))
            api(project(":backup"))
			api(project(":password-strength-estimator"))
            implementation(libs.skie)
        }
    }
}

skie {
    analytics {
        enabled.set(false)
    }
    features {
        group {
            SuppressSkieWarning.NameCollision(true)
        }

        // Enable DefaultArgumentInterop only for our codebase
        // otherwwse it also applies to kotlin code (e.g. kotlinx.serialization) which fails on iOS
        group("ch.admin.foitt") {
            DefaultArgumentInterop.Enabled(true)
        }

        enableSwiftUIObservingPreview = true
        enableFlowCombineConvertorPreview = true
        enableFutureCombineExtensionPreview = true
    }
}
