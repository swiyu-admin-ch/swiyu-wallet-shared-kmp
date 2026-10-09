package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.CredentialDto
import ch.admin.foitt.swiyu.shared.backup.dto.VCDto
import ch.admin.foitt.swiyu.shared.backup.dto.toJwkDto
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.batchVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCHardwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCNoKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.singleVCSoftwareKeyBinding
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.updatedBatchVC
import ch.admin.foitt.swiyu.shared.backup.model.Credential
import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class VerifiableCredentialContainerV1Test {

    @Test
    fun `VC container is correctly constructed for single credentials`() {
        val verifiableCredentials = listOf(
            singleVCNoKeyBinding,
            singleVCHardwareKeyBinding,
            singleVCSoftwareKeyBinding,
        )

        val vcContainer = VerifiableCredentialContainerV1.from(verifiableCredentials)
        assertEquals(VerifiableCredentialContainerV1.TYPE, vcContainer.type)
        assertEquals(verifiableCredentials.size, vcContainer.vcs.size)

        verifiableCredentials.forEachIndexed { index, verifiableCredential ->
            val vc = vcContainer.vcs[index]
            assertVC(verifiableCredential, vc)

            val expectedCredential = verifiableCredential.credentials.first()
            assertCredential(expectedCredential, vc.credentials[0])
        }
    }

    @Test
    fun `VC container is correctly constructed for batch credentials`() {
        val verifiableCredentials = listOf(
            batchVCNoKeyBinding,
            batchVCHardwareKeyBinding,
            batchVCSoftwareKeyBinding,
            updatedBatchVC,
        )

        val vcContainer = VerifiableCredentialContainerV1.from(verifiableCredentials)
        assertEquals(VerifiableCredentialContainerV1.TYPE, vcContainer.type)
        assertEquals(verifiableCredentials.size, vcContainer.vcs.size)

        verifiableCredentials.forEachIndexed { index, verifiableCredential ->
            val vc = vcContainer.vcs[index]
            assertVC(verifiableCredential, vc)

            verifiableCredential.credentials.forEachIndexed { index, expectedCredential ->
                val credential = vc.credentials[index]
                assertCredential(expectedCredential, credential)
            }
        }
    }

    private fun assertVC(vc: VC, vcDto: VCDto) {
        assertEquals(vc.id.toString(), vcDto.id)
        assertEquals(vc.format, vcDto.format)
        assertEquals(vc.credentials.size, vcDto.credentials.size)
    }

    private fun assertCredential(credential: Credential, credentialDto: CredentialDto) {
        assertEquals(credential.id.toString(), credentialDto.id)
        assertEquals(credential.rawCredential, credentialDto.credential)
        assertEquals(credential.jwks?.map { it.toJwkDto() }, credentialDto.jwks)
    }
}