package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.VCDto
import ch.admin.foitt.swiyu.shared.backup.dto.toVCDto
import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
internal class VerifiableCredentialContainerV1 private constructor(
    @SerialName("vcs")
    val vcs: List<VCDto>,
) : Container {

    override val type = TYPE

    companion object {
        const val TYPE = "VerifiableCredentialContainerV1"

        fun from(
            verifiableCredentials: List<VC>
        ): VerifiableCredentialContainerV1 = VerifiableCredentialContainerV1(verifiableCredentials.map { it.toVCDto() })
    }
}
