package ch.admin.foitt.swiyu.shared.backup

import org.kapunsdk.crypto.jwe.encryptJweDirect
import org.kapunsdk.crypto.symmetric.hkdfSha256
import org.kapunsdk.crypto.symmetric.secureRandomBytes

object BackupEncryption {
    const val SALT_SIZE_BYTES = 32

    @Throws(Exception::class)
    fun generateSalt(): ByteArray = secureRandomBytes(SALT_SIZE_BYTES)

    @Throws(Exception::class)
    fun encrypt(content: String, recoveryKey: ByteArray, salt: ByteArray): String {
        val keyBytes = deriveKey(recoveryKey, salt)
        return encryptJweDirect(
            keyBytes,
            content.encodeToByteArray(),
            contentType = "application/json",
        )
    }

    internal fun deriveKey(recoveryKey: ByteArray, salt: ByteArray): ByteArray {
        require(recoveryKey.size == BackupRecoveryKey.SIZE_BYTES) { "Recovery key must contain 32 bytes" }
        require(salt.size == SALT_SIZE_BYTES) { "Salt must contain 32 bytes" }
        return hkdfSha256(recoveryKey, salt)
    }
}
