package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class MetadataDto(
    @SerialName("credential_ids") val credentialIds: List<String>,
    @SerialName("data") val data: String,
)

@OptIn(ExperimentalUuidApi::class)
internal fun List<VC>.toMetadataDtoList(): List<MetadataDto> = this
    .flatMap { it.credentials }
    .groupBy({ it.issuerMetadata }, { it.id.toString() })
    .map { it.toMetadataDto() }

internal fun Map.Entry<String, List<String>>.toMetadataDto() = MetadataDto(
    credentialIds = value,
    data = key
)