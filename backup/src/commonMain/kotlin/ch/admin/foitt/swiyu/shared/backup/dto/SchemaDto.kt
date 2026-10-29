package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
internal data class SchemaDto(
    @SerialName("credential_ids") val credentialIds: List<String>,
    @SerialName("data") val data: String,
)

@OptIn(ExperimentalUuidApi::class)
internal fun List<VC>.toSchemaDtoList(): List<SchemaDto> = this
    .flatMap { it.credentials }
    .groupBy({ it.jsonSchema }, { it.id.toString() })
    .mapNotNull { it.toSchemaDto() }

internal fun Map.Entry<String?, List<String>>.toSchemaDto() = key?.let {
    SchemaDto(
        credentialIds = value,
        data = it,
    )
}
