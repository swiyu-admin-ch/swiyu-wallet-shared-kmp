package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.Credential
import ch.admin.foitt.swiyu.shared.backup.model.Jwk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
internal data class CredentialDto(
    @SerialName("id")
    val id: String,
    @SerialName("credential")
    val credential: String,
    @SerialName("jwks")
    val jwks: List<JwkDto>?,
)

@OptIn(ExperimentalUuidApi::class)
internal fun Credential.toCredentialDto() = CredentialDto(
    id = id.toString(),
    credential = rawCredential,
    jwks = jwks?.map { it.toJwkDto() },
)
