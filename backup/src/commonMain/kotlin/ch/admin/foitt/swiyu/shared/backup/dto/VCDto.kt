package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
internal data class VCDto(
    @SerialName("id")
    val id: String,
    @SerialName("format")
    val format: String,
    @SerialName("credentials")
    val credentials: List<CredentialDto>,
)

@OptIn(ExperimentalUuidApi::class)
internal fun VC.toVCDto() = VCDto(
    id = id.toString(),
    format = format,
    credentials = credentials.map { it.toCredentialDto() },
)
