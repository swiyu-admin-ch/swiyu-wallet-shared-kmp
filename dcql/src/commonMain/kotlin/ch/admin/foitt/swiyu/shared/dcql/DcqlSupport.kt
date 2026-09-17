package ch.admin.foitt.swiyu.shared.dcql

import ch.admin.foitt.swiyu.shared.dcql.model.DcqlClaimValue
import ch.admin.foitt.swiyu.shared.dcql.model.DcqlCredentialMatch
import ch.ubique.heidi.credentials.asSelector
import ch.ubique.heidi.credentials.get
import ch.ubique.heidi.credentials.toClaimsPointer
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import uniffi.heidi_credentials_rust.PointerPart
import uniffi.heidi_dcql_rust.Credential
import uniffi.heidi_dcql_rust.DcqlQuery
import uniffi.heidi_dcql_rust.Disclosure
import uniffi.heidi_dcql_rust.selectCredentialsWithInfo
import uniffi.heidi_util_rust.Value

/**
 * DCQL helper methods that wrap Heidi query matching and VP token generation.
 */
object DcqlSupport {

	init {
		SwissProfileTrustedDidAuthority.register()
	}

	/**
	 * Decodes a DCQL query from JSON.
	 *
	 * @param json DCQL query as JSON string.
	 * @return Parsed DCQL query.
	 */
	@Throws(SerializationException::class)
	fun decodeDcqlQuery(json: String): DcqlQuery =
		Json.decodeFromString<DcqlQuery>(json)

	/**
	 * Matches credential payloads against a DCQL query and returns candidate matches.
	 *
	 * This uses Heidi's DCQL selector to evaluate each payload and then extracts
	 * the original credential payload and resolved claim values for each matching disclosure.
	 *
	 * @param query DCQL query to evaluate.
	 * @param credentialPayloads List of credential payloads (typically SD-JWT or MDOC).
	 * @return A list of matches including query id, payload, and resolved claim values.
	 */
	fun matchDcqlCredentials(
		query: DcqlQuery,
		credentialPayloads: List<String>,
	): List<DcqlCredentialMatch> = runCatching {
		val response = selectCredentialsWithInfo(query, credentialPayloads)
		val matches = mutableListOf<DcqlCredentialMatch>()

		response.setOptions.forEach { credentialSetOption ->
			credentialSetOption.setOptions.forEach { candidateSet ->
				candidateSet.forEach { setOption ->
					setOption.options.forEach { disclosure ->
						val payload = disclosure.extractOriginalPayload() ?: return@forEach
						val claimValues = disclosure.claimsQueries
							.map { claimQuery ->
								DcqlClaimValue(
									paths = claimQuery.path,
									value = resolvePath(disclosure.credential, claimQuery.path)
								)
							}
						matches.add(
							DcqlCredentialMatch(
								credentialQueryId = setOption.id,
								credentialPayload = payload,
								claimValues = claimValues,
							),
						)
					}
				}
			}
		}

		matches
	}.getOrDefault(emptyList())

	fun resolvePath(credential: Credential, path: List<PointerPart>): Value = when (credential) {
		is Credential.SdJwtCredential -> {
			credential.v1.claims.get(path.asSelector()).first()
		}
		is Credential.MdocCredential -> Value.Null
		is Credential.W3cCredential -> Value.Null
		is Credential.BbsCredential -> Value.Null
		is Credential.OpenBadge303Credential -> Value.Null
	}

	private fun Disclosure.extractOriginalPayload(): String? = when (val credential = credential) {
		is Credential.SdJwtCredential -> credential.v1.originalSdjwt
		is Credential.MdocCredential -> credential.v1.originalMdoc
		is Credential.W3cCredential -> credential.v1.originalSdjwt
		is Credential.BbsCredential -> credential.v1.originalBbs
		is Credential.OpenBadge303Credential -> null
	}
}
