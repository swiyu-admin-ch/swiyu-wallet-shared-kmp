package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BackupTest {

    private val recoveryKey = BackupRecoveryKey.generate()

    @Test
    fun `encrypting a backup creates archive when no credentials exist`() {
        val backup = Backup(vcs = emptyList(), directory = "/tmp")

        val archive = backup.encrypt(recoveryKey)

        assertTrue(archive.path.toString().endsWith(".wbak"))
    }

    @Test
    fun `encrypting a backup creates archive for credentials`() {
        val backup = Backup(vcs = listOf(createVc()), directory = "/tmp")

        val archive = backup.encrypt(recoveryKey)

        assertTrue(archive.path.toString().endsWith(".wbak"))
    }

    @Test
    fun `encrypting a backup throws for invalid recovery key`() {
        val backup = Backup(
            vcs = emptyList(),
            directory = "/tmp"
        )

        assertFailsWith<Exception> {
            backup.encrypt("invalid")
        }
    }

    private fun createVc(): VC {
        return VC(
            format = "format",
            credentials = emptyList(),
            refreshToken = null,
            dpopJwk = null,
        )
    }
}