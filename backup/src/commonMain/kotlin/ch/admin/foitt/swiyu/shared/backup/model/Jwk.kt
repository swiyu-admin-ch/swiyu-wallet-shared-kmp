package ch.admin.foitt.swiyu.shared.backup.model

/**
 * Representation of a JWK
 *
 * reference: https://datatracker.ietf.org/doc/html/rfc7517
 */
data class Jwk(
    val crv: String,
    val kty: String,
    val d: String,
    val x: String,
    val y: String? = null,
    val kid: String? = null,
    val use: String? = null,
    val alg: String? = null,
)
