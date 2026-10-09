package ch.admin.foitt.swiyu.shared.backup.dto

import ch.admin.foitt.swiyu.shared.backup.model.Jwk
import kotlinx.serialization.Serializable

/**
 * Representation of a JWK
 *
 * reference: https://datatracker.ietf.org/doc/html/rfc7517
 */
@Serializable
internal data class JwkDto(
    val crv: String,
    val kty: String,
    val d: String,
    val x: String,
    val y: String? = null,
    val kid: String? = null,
    val use: String? = null,
    val alg: String? = null,
)

internal fun Jwk.toJwkDto() = JwkDto(
    crv = crv,
    kty = kty,
    d = d,
    x = x,
    y = y,
    kid = kid,
    use = use,
    alg = alg,
)