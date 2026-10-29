package ch.admin.foitt.swiyu.shared.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class BackupContainerV1Test {

    @OptIn(ExperimentalTime::class)
    @Test
    fun `backup container is correctly constructed`() {
        val fakeInput = "2025-01-01T15:50:30Z"
        val fakeClock = FakeClock(Instant.parse(fakeInput))
        val backupContainerV1 = BackupContainerV1(fakeClock)

        assertEquals(BackupContainerV1.TYPE, backupContainerV1.type)
        assertEquals(fakeInput, backupContainerV1.creationDate)
        assertEquals("swiss-profile-portability:1.0.0", backupContainerV1.profileVersion)
    }

    @OptIn(ExperimentalTime::class)
    class FakeClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }
}
