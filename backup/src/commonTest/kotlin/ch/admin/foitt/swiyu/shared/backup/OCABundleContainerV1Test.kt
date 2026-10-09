package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.OCA_BUNDLE
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.UPDATED_OCA_BUNDLE
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.hardwareKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.noKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.noOCACredential
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.softwareKeyBindingCredential1
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedBatchVC
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedNoKeyBindingCredential
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

class OCABundleContainerV1Test {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `oca bundle container returns null for no OCA bundle`() {
        val vcs = listOf(
            singleVCNoKeyBinding.copy(credentials = listOf(noOCACredential)),
        )

        val ocaContainer = OCABundleContainerV1.from(vcs)
        assertNull(ocaContainer)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `oca bundle container is correctly constructed for single OCA bundle`() {
        val vcs = listOf(
            singleVCNoKeyBinding,
            singleVCHardwareKeyBinding,
            singleVCSoftwareKeyBinding,
        )

        val ocaContainer = OCABundleContainerV1.from(vcs)
        assertNotNull(ocaContainer)
        assertEquals("OCABundleContainerV1", ocaContainer.type)
        assertEquals(1, ocaContainer.bundles.size)

        val bundle = ocaContainer.bundles[0]
        assertEquals(3, bundle.credentialIds.size)
        assertEquals(noKeyBindingCredential1.id.toString(), bundle.credentialIds[0])
        assertEquals(hardwareKeyBindingCredential1.id.toString(), bundle.credentialIds[1])
        assertEquals(softwareKeyBindingCredential1.id.toString(), bundle.credentialIds[2])
        assertEquals(OCA_BUNDLE, bundle.data)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `oca bundle container is correctly constructed for multiple OCA bundles`() {
        val vcs = listOf(
            singleVCHardwareKeyBinding,
            updatedBatchVC,
        )

        val ocaContainer = OCABundleContainerV1.from(vcs)
        assertNotNull(ocaContainer)
        assertEquals("OCABundleContainerV1", ocaContainer.type)
        assertEquals(2, ocaContainer.bundles.size)

        val bundle1 = ocaContainer.bundles[0]
        assertEquals(2, bundle1.credentialIds.size)
        assertEquals(hardwareKeyBindingCredential1.id.toString(), bundle1.credentialIds[0])
        assertEquals(noKeyBindingCredential1.id.toString(), bundle1.credentialIds[1])
        assertEquals(OCA_BUNDLE, bundle1.data)

        val bundle2 = ocaContainer.bundles[1]
        assertEquals(1, bundle2.credentialIds.size)
        assertEquals(updatedNoKeyBindingCredential.id.toString(), bundle2.credentialIds[0])
        assertEquals(UPDATED_OCA_BUNDLE, bundle2.data)
    }
}