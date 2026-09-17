plugins {
	// Kotlin & KMP plugins
    alias(libs.plugins.kotlinMultiplatform) apply false

	// iOS specific plugins
	alias(libs.plugins.skie) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.library) apply false

	// Maven Deployment plugin
	alias(libs.plugins.vanniktech.mavenPublish) apply false
}

val releaseVersion = providers.gradleProperty("releaseVersion")
	.orElse(providers.environmentVariable("RELEASE_VERSION"))
	.map { rawVersion ->
		rawVersion
			.removePrefix("refs/tags/")
			.removePrefix("v")
	}
	.orElse("")

allprojects {
	group = "ch.admin.swiyu.mobile"
	version = releaseVersion.get()
}

val modules = listOf(
	"consistency",
	"dcql",
	"proximity",
	"password-strength-estimator"
)

tasks.register("allTests") {
	group = "verification"
	description = "Runs the tests for all KMP modules and aggregates their reports."
	dependsOn(
		modules.map { moduleName -> ":$moduleName:allTests" }
	)
}

tasks.register("assembleAndroidReleaseArtifacts") {
	group = "build"
	description = "Assembles Android AAR artifacts for all Android KMP modules."
	dependsOn(
		modules.map { moduleName -> ":$moduleName:assembleAndroidMain" }
	)
}

tasks.register("publishAndroidPackages") {
	group = "publishing"
	description = "Publishes selected Android packages to GitHub Packages."
	dependsOn(
		modules
			.map { moduleName -> ":$moduleName:publishAndroidPublicationToGithubPackagesRepository" }
	)
}

tasks.register("publishAndroidPackagesToMavenCentral") {
	group = "publishing"
	description = "Publishes selected Android packages to MavenCentral."
	dependsOn(
		modules
			.map { moduleName -> ":$moduleName:publishToMavenCentral" }
	)
}
