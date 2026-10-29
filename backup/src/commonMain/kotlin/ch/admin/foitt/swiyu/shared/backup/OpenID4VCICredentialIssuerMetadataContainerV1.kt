package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.MetadataDto
import ch.admin.foitt.swiyu.shared.backup.dto.toMetadataDtoList
import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
internal class OpenID4VCICredentialIssuerMetadataContainerV1 private constructor(
    @SerialName("metadata")
    val metadata: List<MetadataDto>,
) : Container {
    override val type = TYPE

    companion object {
        const val TYPE = "OpenID4VCICredentialIssuerMetadataContainerV1"

        fun from(verifiableCredentials: List<VC>): OpenID4VCICredentialIssuerMetadataContainerV1 =
            OpenID4VCICredentialIssuerMetadataContainerV1(
                verifiableCredentials.toMetadataDtoList(),
            )
    }
}
