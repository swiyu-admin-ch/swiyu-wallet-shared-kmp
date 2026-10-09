package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.toJwkDto
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCNoDpopHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCNoDpopNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCNoDpopSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoDpopHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoDpopNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoDpopSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNotRefreshable
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedBatchVC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

class VerifiableCredentialAuthenticationContainerV1Test {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `VC authentication container where no credential is refreshable returns null`() {
        val vcs = listOf(
            singleVCNotRefreshable,
        )

        val authContainer = VerifiableCredentialAuthenticationContainerV1.from(vcs)
        assertNull(authContainer)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `VC authentication container is correctly constructed for credentials`() {
        val vcs = listOf(
            singleVCNoKeyBinding,
            singleVCNoDpopNoKeyBinding,
            singleVCHardwareKeyBinding,
            singleVCNoDpopHardwareKeyBinding,
            singleVCSoftwareKeyBinding,
            singleVCNoDpopSoftwareKeyBinding,
            batchVCNoKeyBinding,
            batchVCNoDpopNoKeyBinding,
            batchVCHardwareKeyBinding,
            batchVCNoDpopHardwareKeyBinding,
            batchVCSoftwareKeyBinding,
            batchVCNoDpopSoftwareKeyBinding,
            updatedBatchVC,
        )

        val authContainer = VerifiableCredentialAuthenticationContainerV1.from(vcs)
        assertNotNull(authContainer)
        assertEquals(VerifiableCredentialAuthenticationContainerV1.TYPE, authContainer.type)
        assertEquals(vcs.size, authContainer.authentications.size)

        vcs.forEachIndexed { index, vc ->
            val auth = authContainer.authentications[index]
            assertEquals(vc.id.toString(), auth.vcId)
            assertEquals(vc.refreshToken, auth.refreshToken)
            assertEquals(vc.dpopJwk?.toJwkDto(), auth.dpopJwk)
        }
    }
}