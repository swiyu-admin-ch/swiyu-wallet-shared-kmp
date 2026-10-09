package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.model.PlatformFile
import ch.admin.foitt.swiyu.shared.backup.model.VC
import ch.admin.foitt.swiyu.shared.backup.util.writeFile
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

/**
 * Backup Container used to create all files that are needed to back up all credentials
 *
 * @param vcs List of verifiable credentials to back up
 * @param directory working directory.
 *
 * Directory examples:
 * Android: context.filesDir.path
 * iOS: NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first
 */
class Backup(
    private val vcs: List<VC>,
    private val directory: String,
) {
    val json = Json {
        explicitNulls = false
    }

    private val metaFile = createMetaFile()
    private val wbakFiles = createWbakFiles()

    /**
     * creates meta.json file contents for backup:
     *
     * reference: https://github.com/swiyu-admin-ch/community/blob/main/specifications/wallet-backup-container/wallet-backup-container.md
     */
    @OptIn(ExperimentalTime::class)
    private fun createMetaFile(): PlatformFile {
        val backupContainerV1 = BackupContainerV1()
        val content = json.encodeToString(backupContainerV1)

        return writeFile(directory = directory, fileName = "meta.json", content = content)
    }

    /**
     * creates all needed wbak-N.json file contents for backup:
     * - VerifiableCredentialContainerV1
     * - VerifiableCredentialAuthenticationContainerV1
     * - VerifiableCredentialJSONSchemaContainerV1
     * - OpenID4VCICredentialIssuerMetadataContainerV1
     * - OCABundleContainerV1
     *
     * reference: https://github.com/swiyu-admin-ch/community/blob/main/specifications/wallet-backup-container/wallet-backup-container.md
     */
    @OptIn(ExperimentalTime::class)
    private fun createWbakFiles(): List<PlatformFile> {
        if (vcs.isEmpty()) return emptyList()

        return listOf(
            VerifiableCredentialContainerV1.from(vcs),
            VerifiableCredentialAuthenticationContainerV1.from(vcs),
            VerifiableCredentialJSONSchemaContainerV1.from(vcs),
            OpenID4VCICredentialIssuerMetadataContainerV1.from(vcs),
            OCABundleContainerV1.from(vcs),
        ).mapIndexedNotNull { index, container ->
            container?.let {
                val content = json.encodeToString(it)
                writeFile(directory = directory, fileName = "wbak-$index.json", content = content)
            }
        }
    }


    /**
     * encrypt created backup files
     */
    fun encrypt() {
        // TODO: EIDNUCLEUS-1305
    }
}