package ch.admin.foitt.swiyu.shared.backup.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Representation of a verifiable credential as a whole
 *
 * @param format Format of the VC. Example: dc+sd-jwt
 * @param credentials List of [Credential]s.
 * @param refreshToken Token used to refresh the credential.
 * @param dpopJwk DPoP private key JWK.
 */
data class VC(
    val format: String,
    val credentials: List<Credential>,
    val refreshToken: String?,
    val dpopJwk: Jwk?,
) {
    @OptIn(ExperimentalUuidApi::class)
    val id: Uuid = Uuid.random()
}