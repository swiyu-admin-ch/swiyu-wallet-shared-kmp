buildscript {
	dependencies {
		classpath(libs.detekt.gradle.plugin)
	}
}

plugins {
	// Kotlin & KMP plugins
    alias(libs.plugins.kotlinMultiplatform) apply false
	alias(libs.plugins.android.kotlin.multiplatform.library) apply false
	alias(libs.plugins.uniffi.plugin) apply false
	// iOS specific plugins
	alias(libs.plugins.skie) apply false


	// Maven Deployment plugin
	alias(libs.plugins.vanniktech.mavenPublish) apply false

	// detekt build-logic plugin -- apply directly on root level
	alias(libs.plugins.swiyu.kmp.detekt)
}

val releaseVersion = providers.gradleProperty("APP_VERSION_NAME")
	.orElse(providers.environmentVariable("APP_VERSION_NAME"))
	.map { rawVersion ->
		rawVersion
			.removePrefix("refs/tags/")
			.removePrefix("v")
	}
	.orElse("")

allprojects {
	version = releaseVersion.get()
}

val modules = listOf(
	"consistency",
	"dcql",
	"proximity",
	"password-strength-estimator",
	"backup"
)

tasks.register("allTests") {
	group = "verification"
	description = "Runs the tests for all KMP modules and aggregates their reports."
	dependsOn(
		modules.map { moduleName -> ":$moduleName:allTests" }
	)
}

modules.forEach { moduleName ->
	project(":$moduleName") {
		tasks.matching { it.name == "allTests" }.configureEach {
			finalizedBy(
				tasks.matching { it.name == "koverHtmlReport" },
				tasks.matching { it.name == "koverXmlReport" }
			)
		}
	}
}
