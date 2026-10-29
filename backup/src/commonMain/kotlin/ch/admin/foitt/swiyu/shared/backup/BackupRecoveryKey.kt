package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.util.base64Url
import org.kapunsdk.crypto.symmetric.secureRandomBytes

object BackupRecoveryKey {
    internal const val SIZE_BYTES = 32

    @Throws(Exception::class)
    fun generate(): String {
        val bytes = secureRandomBytes(SIZE_BYTES)
        return base64Url.encode(bytes)
    }

    internal fun decode(value: String): ByteArray {
        return base64Url.decode(value)
    }
}
