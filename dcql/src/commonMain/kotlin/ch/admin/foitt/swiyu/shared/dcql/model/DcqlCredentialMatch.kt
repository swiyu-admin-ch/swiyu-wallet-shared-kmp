package ch.admin.foitt.swiyu.shared.dcql.model

/**
 * Result of matching a DCQL query against a list of credential payloads.
 *
 * @property credentialQueryId The id of the credential query that matched.
 * @property credentialPayload The original credential payload selected for the match.
 * @property claimValues Resolved claim values requested by the query.
 */
data class DcqlCredentialMatch(
    val credentialQueryId: String,
    val credentialPayload: String,
    val claimValues: List<DcqlClaimValue>,
)
