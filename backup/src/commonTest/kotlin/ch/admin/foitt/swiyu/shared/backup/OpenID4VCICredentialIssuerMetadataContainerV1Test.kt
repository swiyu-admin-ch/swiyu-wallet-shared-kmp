package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.ISSUER_METADATA
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.UPDATED_ISSUER_METADATA
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
import kotlin.uuid.ExperimentalUuidApi

class OpenID4VCICredentialIssuerMetadataContainerV1Test {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `issuer metadata container is correctly constructed for single issuer metadata`() {
        val vcs = listOf(
            singleVCNoKeyBinding,
            singleVCHardwareKeyBinding,
            singleVCSoftwareKeyBinding,
        )

        val metadataContainer = OpenID4VCICredentialIssuerMetadataContainerV1.from(vcs)
        assertEquals(OpenID4VCICredentialIssuerMetadataContainerV1.TYPE, metadataContainer.type)
        assertEquals(1, metadataContainer.metadata.size)

        val metadata = metadataContainer.metadata[0]
        assertEquals(3, metadata.credentialIds.size)
        assertEquals(noKeyBindingCredential1.id.toString(), metadata.credentialIds[0])
        assertEquals(hardwareKeyBindingCredential1.id.toString(), metadata.credentialIds[1])
        assertEquals(softwareKeyBindingCredential1.id.toString(), metadata.credentialIds[2])
        assertEquals(ISSUER_METADATA, metadata.data)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `issuer metadata container is correctly constructed for multiple issuer metadata`() {
        val vcs = listOf(
            singleVCHardwareKeyBinding,
            updatedBatchVC,
        )

        val metadataContainer = OpenID4VCICredentialIssuerMetadataContainerV1.from(vcs)
        assertEquals(OpenID4VCICredentialIssuerMetadataContainerV1.TYPE, metadataContainer.type)
        assertEquals(2, metadataContainer.metadata.size)

        val metadata1 = metadataContainer.metadata[0]
        assertEquals(2, metadata1.credentialIds.size)
        assertEquals(hardwareKeyBindingCredential1.id.toString(), metadata1.credentialIds[0])
        assertEquals(noKeyBindingCredential1.id.toString(), metadata1.credentialIds[1])
        assertEquals(ISSUER_METADATA, metadata1.data)

        val metadata2 = metadataContainer.metadata[1]
        assertEquals(1, metadata2.credentialIds.size)
        assertEquals(updatedNoKeyBindingCredential.id.toString(), metadata2.credentialIds[0])
        assertEquals(UPDATED_ISSUER_METADATA, metadata2.data)
    }
}