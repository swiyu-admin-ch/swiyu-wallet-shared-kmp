package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.SchemaDto
import ch.admin.foitt.swiyu.shared.backup.dto.toSchemaDtoList
import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
internal class VerifiableCredentialJSONSchemaContainerV1 private constructor(
    @SerialName("schemas")
    val schemas: List<SchemaDto>,
) : Container {
    override val type = TYPE

    companion object {
        const val TYPE = "VerifiableCredentialJSONSchemaContainerV1"

        fun from(
            verifiableCredentials: List<VC>
        ): VerifiableCredentialJSONSchemaContainerV1 = VerifiableCredentialJSONSchemaContainerV1(
            verifiableCredentials.toSchemaDtoList()
        )
    }
}
