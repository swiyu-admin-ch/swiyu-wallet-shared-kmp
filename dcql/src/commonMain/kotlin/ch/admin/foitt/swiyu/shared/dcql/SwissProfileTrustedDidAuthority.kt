package ch.admin.foitt.swiyu.shared.dcql

import org.kapunsdk.SdJwtCredential
import uniffi.kapun_crypto_rust.getKidFromJwt
import uniffi.kapun_dcql_rust.Credential
import uniffi.kapun_dcql_rust.TrustedAuthority
import uniffi.kapun_dcql_rust.TrustedAuthorityMatcher
import uniffi.kapun_dcql_rust.TrustedAuthorityQueryType
import uniffi.kapun_dcql_rust.registerMatcher

object SwissProfileTrustedDidAuthority : TrustedAuthorityMatcher {

	override fun id() = "SwissProfileDidTrustedAuthorityMatcher"

	override fun matches(
		value: Credential,
		trustedAuthority: TrustedAuthority,
	): Boolean? {
		return when (value) {
			is Credential.SdJwtCredential -> {
				val sdJwt = (value.v1 as? SdJwtCredential)?.sdjwt ?: return null
				val kid = getKidFromJwt(sdJwt.originalJwt)
				val did = kid?.let {
					getDidFromAbsoluteKid(kid)
				}  ?: return false
				trustedAuthority.values.contains(did)
			}
			else -> null
		}
	}

	override fun queryType() = TrustedAuthorityQueryType.DECENTRALIZED_IDENTIFIER

	private fun getDidFromAbsoluteKid(kid: String) : String? {
		val occurencesHashTag = kid.count { it == '#'}
		// we need exactly one hashtag
		if(occurencesHashTag != 1) {
			return null
		}
		// left split on hashtag
		val didKid = kid.split("#", limit = 2)
		if(didKid.size != 2) {
			return null
		}
		return didKid[0]
	}

	fun register() {
		registerMatcher(this)
	}
}