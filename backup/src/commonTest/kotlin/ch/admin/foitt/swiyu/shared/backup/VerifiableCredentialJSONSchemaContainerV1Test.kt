package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.JSON_SCHEMA
import ch.admin.foitt.swiyu.shared.backup.mock.ContainerMocks.UPDATED_JSON_SCHEMA
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

class VerifiableCredentialJSONSchemaContainerV1Test {

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `json schema container is correctly constructed for single JSON schema`() {
        val vcs = listOf(
            singleVCNoKeyBinding,
            singleVCHardwareKeyBinding,
            singleVCSoftwareKeyBinding,
        )

        val jsonSchemaContainer = VerifiableCredentialJSONSchemaContainerV1.from(vcs)
        assertEquals(VerifiableCredentialJSONSchemaContainerV1.TYPE, jsonSchemaContainer.type)
        assertEquals(1, jsonSchemaContainer.schemas.size)

        val schema = jsonSchemaContainer.schemas[0]
        assertEquals(3, schema.credentialIds.size)
        assertEquals(noKeyBindingCredential1.id.toString(), schema.credentialIds[0])
        assertEquals(hardwareKeyBindingCredential1.id.toString(), schema.credentialIds[1])
        assertEquals(softwareKeyBindingCredential1.id.toString(), schema.credentialIds[2])
        assertEquals(JSON_SCHEMA, schema.data)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `json schema container is correctly constructed for multiple JSON schema`() {
        val vcs = listOf(
            singleVCHardwareKeyBinding,
            updatedBatchVC,
        )

        val jsonSchemaContainer = VerifiableCredentialJSONSchemaContainerV1.from(vcs)
        assertEquals(VerifiableCredentialJSONSchemaContainerV1.TYPE, jsonSchemaContainer.type)
        assertEquals(2, jsonSchemaContainer.schemas.size)

        val schema1 = jsonSchemaContainer.schemas[0]
        assertEquals(2, schema1.credentialIds.size)
        assertEquals(hardwareKeyBindingCredential1.id.toString(), schema1.credentialIds[0])
        assertEquals(noKeyBindingCredential1.id.toString(), schema1.credentialIds[1])
        assertEquals(JSON_SCHEMA, schema1.data)

        val schema2 = jsonSchemaContainer.schemas[1]
        assertEquals(1, schema2.credentialIds.size)
        assertEquals(updatedNoKeyBindingCredential.id.toString(), schema2.credentialIds[0])
        assertEquals(UPDATED_JSON_SCHEMA, schema2.data)
    }
}