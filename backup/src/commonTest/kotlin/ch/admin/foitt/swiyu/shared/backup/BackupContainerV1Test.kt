package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.OCA_BUNDLE
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.UPDATED_OCA_BUNDLE
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.hardwareKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.noKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.softwareKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedBatchVC
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedNoKeyBindingCredential
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class BackupContainerV1Test {

    @OptIn(ExperimentalTime::class)
    @Test
    fun `backup container is correctly constructed`() {
        val fakeInput = "2025-01-01T15:50:30Z"
        val fakeClock = FakeClock(Instant.parse(fakeInput))
        val backupContainerV1 = BackupContainerV1(fakeClock)

        assertEquals(BackupContainerV1.TYPE, backupContainerV1.type)
        assertEquals(fakeInput, backupContainerV1.creationDate)
    }

    @OptIn(ExperimentalTime::class)
    class FakeClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }
}