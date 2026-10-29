package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.util.base64Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BackupRecoveryKeyTest {

    @Test
    fun `recovery key is uniquely generated`() {
        val recoveryKey1 = BackupRecoveryKey.generate()
        val recoveryKey2 = BackupRecoveryKey.generate()

        val keyBytes = BackupRecoveryKey.decode(recoveryKey1)
        assertEquals(43, recoveryKey1.length)
        assertEquals(32, keyBytes.size)
        assertEquals(recoveryKey1, base64Url.encode(keyBytes))
        assertNotEquals(recoveryKey1, recoveryKey2)
    }
}
