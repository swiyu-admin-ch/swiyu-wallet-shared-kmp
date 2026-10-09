package ch.admin.foitt.swiyu.shared.backup.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Representation of a single raw credential, f. e. a item of a batch
 *
 * @param rawCredential Raw sd-jwt. Example: <header>.<body>.<signature>~disclosure1~...~disclosureN~
 * @param jwks List of private key JWKs used for holder binding proofs
 * @param jsonSchema base64URL encoded String of the JSON Schema of the credential
 * @param issuerMetadata base64URL encoded String of the issuer metadata of the credential
 * @param ocaBundle base64URL encoded String of the OCA bundle of the credential
 */
data class Credential(
    val rawCredential: String,
    val jwks: List<Jwk>?,
    val jsonSchema: String,
    val issuerMetadata: String,
    val ocaBundle: String? = null,
) {
    @OptIn(ExperimentalUuidApi::class)
    val id: Uuid = Uuid.random()
}
