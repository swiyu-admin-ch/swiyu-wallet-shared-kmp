package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.AuthenticationDto
import ch.admin.foitt.swiyu.shared.backup.dto.toAuthenticationDto
import ch.admin.foitt.swiyu.shared.backup.model.VC
import ch.admin.foitt.swiyu.shared.backup.util.ifNotEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
internal class VerifiableCredentialAuthenticationContainerV1 private constructor(
    @SerialName("authentications")
    val authentications: List<AuthenticationDto>,
) : Container {
    override val type = TYPE

    companion object {
        const val TYPE = "VerifiableCredentialAuthenticationContainerV1"

        fun from(verifiableCredentials: List<VC>): VerifiableCredentialAuthenticationContainerV1? =
            verifiableCredentials
                .mapNotNull { it.toAuthenticationDto() }
                .ifNotEmpty { authenticationDtos ->
                    VerifiableCredentialAuthenticationContainerV1(authenticationDtos)
                }
    }
}
