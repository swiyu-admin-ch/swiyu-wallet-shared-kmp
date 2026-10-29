package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.model.BackupFile
import ch.admin.foitt.swiyu.shared.backup.model.BackupFile.Extension
import ch.admin.foitt.swiyu.shared.backup.model.PlatformFile
import ch.admin.foitt.swiyu.shared.backup.model.VC
import ch.admin.foitt.swiyu.shared.backup.util.base64Url
import ch.admin.foitt.swiyu.shared.backup.util.writeBackupArchive
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
class Backup(private val vcs: List<VC>, private val directory: String) {
    private val json = Json {
        explicitNulls = false
        encodeDefaults = true
    }

    /**
     * encrypt created backup files
     */
    @Throws(Exception::class)
    fun encrypt(recoveryKey: String): PlatformFile {
        val keyBytes = BackupRecoveryKey.decode(recoveryKey)
        val metaFile = createMetaFile()
        val wbakFiles = createWbakFiles()
        val salts = mutableMapOf<String, String>()

        val encryptedFiles = wbakFiles.map { file ->
            val salt = BackupEncryption.generateSalt()
            val content = BackupEncryption.encrypt(file.content, keyBytes, salt)
            val encryptedFile = file.copy(extension = Extension.JWE, content = content)
            salts[encryptedFile.fileName] = base64Url.encode(salt)

            encryptedFile
        }

        val containerEncryptionFile = createContainerEncryptionFile(salts)

        return writeBackupArchive(directory, listOf(metaFile, containerEncryptionFile) + encryptedFiles)
    }

    /**
     * creates meta.json file contents for backup:
     *
     * reference: https://github.com/swiyu-admin-ch/community/blob/main/specifications/wallet-backup-container/wallet-backup-container.md
     */
    @OptIn(ExperimentalTime::class)
    private fun createMetaFile(): BackupFile {
        val backupContainerV1 = BackupContainerV1()
        val content = json.encodeToString(backupContainerV1)

        return BackupFile(name = "meta", extension = Extension.JSON, content = content)
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
    private fun createWbakFiles(): List<BackupFile> {
        if (vcs.isEmpty()) return emptyList()

        return listOfNotNull(
            json.encodeToString(VerifiableCredentialContainerV1.from(vcs)),
            VerifiableCredentialAuthenticationContainerV1.from(vcs)?.let { json.encodeToString(it) },
            json.encodeToString(VerifiableCredentialJSONSchemaContainerV1.from(vcs)),
            json.encodeToString(OpenID4VCICredentialIssuerMetadataContainerV1.from(vcs)),
            OCABundleContainerV1.from(vcs)?.let { json.encodeToString(it) },
        ).mapIndexed { index, content ->
            BackupFile(name = "wbak-$index", extension = Extension.JSON, content = content)
        }
    }

    private fun createContainerEncryptionFile(salts: Map<String, String>): BackupFile {
        val containerEncryptionContainerV1 = ContainerEncryptionContainerV1(salts)
        val content = json.encodeToString(containerEncryptionContainerV1)

        return BackupFile(name = "container_encryption", extension = Extension.JSON, content = content)
    }
}
