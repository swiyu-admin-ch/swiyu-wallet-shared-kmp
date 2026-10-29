package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
internal data class AuthenticationDto(
    @SerialName("vc_id")
    val vcId: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("dpop_jwk")
    val dpopJwk: JwkDto? = null,
)

@OptIn(ExperimentalUuidApi::class)
internal fun VC.toAuthenticationDto() = refreshToken?.let { token ->
    AuthenticationDto(
        vcId = id.toString(),
        refreshToken = token,
        dpopJwk = dpopJwk?.toJwkDto(),
    )
}
