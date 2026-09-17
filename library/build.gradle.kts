import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.SuppressSkieWarning
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.skie)
}

kotlin {
    androidLibrary {
        namespace = "ch.admin.foitt.swiyu.shared.kmp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

	jvm()

	val xcf = XCFramework()
	val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

	iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "BITSwiyuSharedKMP"
            isStatic = true
			export(project(":dcql"))
			export(project(":proximity"))
			export(project(":consistency"))
			export(project(":password-strength-estimator"))
			export(libs.heidi.proximity)

            // Ensure swiftinterface generation and stable bundle id.
            binaryOption("bundleId", "ch.admin.foitt.swiyu.shared.kmp")
            freeCompilerArgs += listOf("-Xbinary=bundleId=ch.admin.foitt.swiyu.shared.kmp")

			xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
			api(project(":dcql"))
			api(project(":proximity"))
			api(project(":consistency"))
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
			DefaultArgumentInterop.Enabled(true)
		}
		enableSwiftUIObservingPreview = true
		enableFlowCombineConvertorPreview = true
		enableFutureCombineExtensionPreview = true
	}
}
