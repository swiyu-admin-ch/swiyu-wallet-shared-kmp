package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.dto.toBundleDto
import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
internal data class BundleDto(
    @SerialName("credential_ids") val credentialIds: List<String>,
    @SerialName("data") val data: String,
)

@OptIn(ExperimentalUuidApi::class)
internal fun List<VC>.toBundleDtoList(): List<BundleDto> = this
    .flatMap { it.credentials }
    .groupBy({ it.ocaBundle }, { it.id.toString() })
    .mapNotNull { it.toBundleDto() }

internal fun Map.Entry<String?, List<String>>.toBundleDto() = key?.let {
    BundleDto(
        credentialIds = value,
        data = it
    )
}