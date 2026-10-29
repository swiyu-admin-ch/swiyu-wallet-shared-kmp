import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import project.convention.logic.ModuleVersionExtension
import project.convention.logic.plugin
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

private const val PROPERTY_PUBLISH_TARGET = "publishTarget"
private const val PROPERTY_AUTOMATIC_RELEASE = "mavenCentralAutomaticPublishing"

private enum class PublishingTarget(val value: String) {
    MAVEN_LOCAL("mavenLocal"),
    MAVEN_CENTRAL("mavenCentral"),
    BUILD_ONLY("buildOnly");

    companion object {
        fun find(value: String?): PublishingTarget =
            PublishingTarget.entries.find { it.value == value } ?: BUILD_ONLY
    }
}

private const val ANDROID_PUBLICATION_NAME = "android"
private const val POM_URL = "https://github.com/swiyu-admin-ch/swiyu-wallet-shared-kmp"

/**
 * Sets up Maven publishing for a KMP library module.
 *
 * The vanniktech `com.vanniktech.maven.publish` plugin generates the pom, sources and javadoc artifacts
 * automatically. The deployment target is selected via `-PpublishTarget=`: `mavenLocal`,
 * `mavenCentral` or `buildOnly`.
 *
 * The [publishMaven] task builds the module's android artifact (including the sources jar) and publishes
 * it to the selected target. An invalid `-PpublishTarget=` falls back to `buildOnly`
 *
 * ```
 * ./gradlew :backup:publishMaven -PpublishTarget=mavenLocal
 * ./gradlew :backup:publishMaven -PpublishTarget=mavenCentral
 * ./gradlew :backup:publishMaven -PpublishTarget=buildOnly
 * ```
 *
 * No publishTarget is the same as `buildOnly`
 * ```
 * ./gradlew :backup:publishMaven
 * ```
 *
 * Gradle properties passed on the command line flow into the artifact producing tasks.
 *
 * Maven Central is immutable: publishing a version that is already released gets rejected. When
 * publishing to Maven Central the plugin therefore checks whether the version is already published
 * and skips the module with a warning instead of failing.
 *
 * When `-PmavenCentralAutomaticPublishing=true` is set (also readable via `ORG_GRADLE_PROJECT_mavenCentralAutomaticPublishing`
 * or `gradle.properties`), the deployment is uploaded to the Central Portal as AUTOMATIC and released
 * without a manual publishing step on the portal website. Modules skipped by the already-published guard
 * are not affected.
 *
 * Each module tracks its own version in libs.versions.toml under its module name, so modules can be published
 * independently of each other:
 *
 * ```
 * # gradle/libs.versions.toml
 * <module> = "0.3.0"
 * ```
 *
 * The module version always wins over `APP_VERSION_NAME` property, so bump it before publishing the module.
 */
class MavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(ModuleVersionConventionPlugin::class.java)
                apply(plugin("vanniktech-mavenPublish"))
            }

            val rawPublishTarget = providers.gradleProperty(PROPERTY_PUBLISH_TARGET).orNull?.trim()
            val publishTarget = PublishingTarget.find(rawPublishTarget)
            if (rawPublishTarget != null && publishTarget == PublishingTarget.BUILD_ONLY) {
                logger.warn("Unknown -P$PROPERTY_PUBLISH_TARGET='$rawPublishTarget', falling back to buildOnly.")
            }

            val automaticRelease =
                providers.gradleProperty(PROPERTY_AUTOMATIC_RELEASE).orNull?.toBoolean() ?: false

            val projectName = name

            // The extension value is set by the module's build script, which runs AFTER this
            // plugin is applied, so grab the Provider now and resolve it later.
            val moduleVersion = extensions
                .getByType(ModuleVersionExtension::class.java)
                .moduleVersion

            afterEvaluate {
                val projectVersion = resolveModuleVersion(moduleVersion)
                version = projectVersion
                extensions.configure<MavenPublishBaseExtension> {
                    coordinates("ch.admin.swiyu.mobile", projectName, projectVersion)
                    publishToMavenCentral(
                        automaticRelease = automaticRelease,
                        validateDeployment = DeploymentValidation.NONE,
                    )

                    // Signing is only enforced when publishing to Maven Central or when signing credentials are
                    // configured, so that publishing to mavenLocal also works without a GPG key.
                    if (publishTarget == PublishingTarget.MAVEN_CENTRAL && !hasSigningCredentials()) {
                        throw GradleException(
                            "publishTarget=mavenCentral requires GPG signing credentials."
                        )
                    }
                    if (publishTarget == PublishingTarget.MAVEN_CENTRAL || hasSigningCredentials()) {
                        signAllPublications()
                    }

                    pom {
                        name.set(projectName)
                        description.set(
                            target.description
                                ?: "$projectName library used in the Swiyu Android wallet"
                        )
                        url.set(POM_URL)
                        inceptionYear.set("2026")
                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                id.set("swiyu-admin-ch")
                                name.set("swiyu - the Swiss Trust Infrastructure ecosystem")
                                url.set("https://github.com/swiyu-admin-ch")
                            }
                        }
                        scm {
                            url.set(POM_URL)
                        }
                    }
                }
            }

            registerPublishMavenTask(publishTarget)

            if (publishTarget == PublishingTarget.MAVEN_CENTRAL) {
                registerMavenCentralPublishedGuard()
            }
        }
    }

    /**
     * Maven Central rejects publishing a version that is already released, and the vanniktech central
     * flow registers its end-of-build upload as soon as the prepare task runs. When the module's version
     * is already published, the maven central publishing is therefore not wired at all - the artifact is
     * only built and a warning is printed.
     */
    private fun Project.registerMavenCentralPublishedGuard() {
        afterEvaluate {
            val androidPublication = extensions
                .getByType(PublishingExtension::class.java).publications
                .findByName(ANDROID_PUBLICATION_NAME) as? MavenPublication
                ?: return@afterEvaluate

            val isPublished = isVersionPublished(
                groupId = androidPublication.groupId,
                artifactId = androidPublication.artifactId,
                version = androidPublication.version,
            )
            if (isPublished) {
                logger.warn(
                    "${androidPublication.groupId}:${androidPublication.artifactId}:" +
                            "${androidPublication.version} is already published on Maven Central - skipping publishing."
                )
                tasks.named("publishMaven") {
                    setDependsOn(listOf("assembleAndroidMain"))
                }
            }
        }
    }

    // Maven Central serves all released artifacts from repo1.maven.org; a resolvable pom means the
    // version is already published. On network problems assume it is not published so the upload is
    // attempted (and fails as it would without the guard).
    private fun isVersionPublished(groupId: String, artifactId: String, version: String): Boolean {
        val mavenCentralReleaseUrl = "https://repo1.maven.org/maven2"
        val modulePath = groupId.replace('.', '/')
        val pomUrl =
            "$mavenCentralReleaseUrl/$modulePath/$artifactId/$version/$artifactId-$version.pom"
        var connection: HttpURLConnection? = null
        return try {
            connection = URI(pomUrl).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: IOException) {
            false
        } finally {
            connection?.disconnect()
        }
    }

    private fun Project.resolveModuleVersion(moduleVersion: Provider<String>): String {
        val resolved = moduleVersion.orNull ?: ""

        if (resolved.isBlank()) {
            throw GradleException(
                "No version resolved for $path. Set the version in the moduleVersion { } block in the module's build.gradle.kts."
            )
        }
        return resolved
    }

    private fun Project.hasSigningCredentials(): Boolean =
        listOf("signingInMemoryKey", "signingKey", "signing.keyId")
            .any { !providers.gradleProperty(it).orNull.isNullOrBlank() }

    private fun Project.registerPublishMavenTask(publishTarget: PublishingTarget) {
        tasks.register("publishMaven") {
            group = "publishing"
            description = when (publishTarget) {
                PublishingTarget.MAVEN_LOCAL ->
                    "Builds the module's android artifact (incl. sources jar) and publishes it to mavenLocal."

                PublishingTarget.MAVEN_CENTRAL ->
                    "Builds the module's android artifact (incl. sources jar) and publishes it to Maven Central."

                PublishingTarget.BUILD_ONLY ->
                    "Builds the module's android artifact. Set -$PROPERTY_PUBLISH_TARGET= to also publish it."
            }

            when (publishTarget) {
                PublishingTarget.MAVEN_LOCAL -> dependsOn("publishAndroidPublicationToMavenLocal")
                PublishingTarget.MAVEN_CENTRAL -> dependsOn("publishAndroidPublicationToMavenCentralRepository")
                PublishingTarget.BUILD_ONLY -> dependsOn("assembleAndroidMain")
            }
        }
    }
}
