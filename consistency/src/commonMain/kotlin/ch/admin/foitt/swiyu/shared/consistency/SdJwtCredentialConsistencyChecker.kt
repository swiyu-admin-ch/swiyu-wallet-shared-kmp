package ch.admin.foitt.swiyu.shared.consistency

import ch.ubique.heidi.credentials.SdJwt
import ch.ubique.heidi.util.extensions.asArray
import ch.ubique.heidi.util.extensions.asObject
import ch.ubique.heidi.util.extensions.get
import ch.ubique.heidi.util.extensions.isArray
import ch.ubique.heidi.util.extensions.isObject
import ch.ubique.heidi.util.extensions.isSame
import uniffi.heidi_crypto_rust.getKidFromJwt
import uniffi.heidi_util_rust.Value

class SdJwtCredentialConsistencyChecker(
	// Claims that MUST differ, e.g. the confirmation key must be distinct
	val mustBeDifferent: List<String> = listOf("cnf", "status"),
	// Which claims should be skipped, essentially meta claims (especially _sd)
	val skippedClaims: List<String> = listOf("exp", "iat", "nbf", "_sd"),
) {
	enum class ConsistencyResult {
		Ok,
		Warn,
		Error
	}

	/**
	 * Checks consistency of two SD-JWTs. Consistency means:
	 * - Have the same "user-claims"
	 * - Have different cryptographic key material
	 * - Have different salts for the disclosures
	 *
	 * @param left SD-JWT to compare
	 * @param right SD-JWT to compare
	 *
	 * @return [ConsistencyResult] showing if the two SD-JWTs are consistent according to the rules above.
	 */
	fun checkConsistency(left: String, right: String): ConsistencyResult {
		val sdjwt1 = runCatching { SdJwt.parse(left) }.getOrNull() ?: return ConsistencyResult.Error
		val sdjwt2 = runCatching { SdJwt.parse(right) }.getOrNull() ?: return ConsistencyResult.Error
		val kid1 = runCatching { getKidFromJwt(sdjwt1.innerJwt.originalJwt) }.getOrNull() ?: return ConsistencyResult.Error
		val kid2 = runCatching { getKidFromJwt(sdjwt2.innerJwt.originalJwt) }.getOrNull() ?: return ConsistencyResult.Error
		if(!sameDids(kid1, kid2)) {
			return ConsistencyResult.Error
		}
		val disclosureConsistency = checkDisclosureConsistency(sdjwt1, sdjwt2)
		// We check the disclosures separately
		when (disclosureConsistency) {
			ConsistencyResult.Ok -> {}
			ConsistencyResult.Warn -> {}
			ConsistencyResult.Error -> return disclosureConsistency
		}
		// if the two objects differ in size they can't be the same object
		if (sdjwt1.innerJwt.claims.asObject()?.size != sdjwt2.innerJwt.claims.asObject()?.size) {
			return ConsistencyResult.Error
		}
		// check all object entries of left and compare them to right
		//
		// Note: we assume that, since we checked the size above, it is enough to do this loop once
		// theoretically one could create an attack, (ab)using the skipped claims to emit same size overall
		// but have a different number of fields being compared.
		// If this is an actual attack vector, we can do the same but switch left and right (see below):

		for (entry in sdjwt1.innerJwt.claims.asObject() ?: return ConsistencyResult.Error) {
			val value2 = sdjwt2.innerJwt.claims[entry.key]
			// check for differences first
			if (mustBeDifferent.contains(entry.key)) {
				if (entry.value.isSame(value2)) {
					return ConsistencyResult.Error
				} else {
					continue
				}
			}
			// skip claims to be ignored
			if (skippedClaims.contains(entry.key)) {
				continue
			}
			// check inner objects with the same rules as here (skipping and equality)
			if (entry.value.isObject()) {
				val objectConsistency = checkObjectConsistency(entry.value, value2)
				when (objectConsistency) {
					ConsistencyResult.Ok -> continue
					ConsistencyResult.Warn -> continue
					ConsistencyResult.Error -> return objectConsistency
				}
			}
			// check arrays element-wise, recursing so nested objects/arrays follow
			// the same skipping rules (e.g. per-element _sd disclosures)
			if (entry.value.isArray()) {
				val arrayConsistency = checkArrayConsistency(entry.value, value2)
				when (arrayConsistency) {
					ConsistencyResult.Ok -> continue
					ConsistencyResult.Warn -> continue
					ConsistencyResult.Error -> return arrayConsistency
				}
			}
			// all other elements must be compared to be equal!
			if (!entry.value.isSame(value2)) {
				return ConsistencyResult.Error
			}
		}
		//val inverseCheckResult = checkConsistency(sdjwt2.innerJwt.originalSdjwt, sdjwt1.innerJwt.originalSdjwt)
		//when (inverseCheckResult) {
		//	ConsistencyResult.Ok -> {}
		//	ConsistencyResult.Warn -> {}
		//	ConsistencyResult.Error -> return inverseCheckResult
		//}
		return ConsistencyResult.Ok
	}

	/**
	 * Basic check if the did, extracted from an absolute kid, is the same.
	 */
	fun sameDids(kid1: String, kid2: String) : Boolean {
		val did1Parts = kid1.split("#", limit = 2)
		if(did1Parts.size != 2) {
			return false
		}
		if(did1Parts[1].contains("#")) {
			return false
		}
		val did2Parts = kid2.split("#", limit = 2)
		if(did2Parts.size != 2) {
			return false
		}
		if(did2Parts[1].contains("#")) {
			return false
		}
		return did1Parts[0] == did2Parts[0]
	}

	/**
	 * Checks the disclosure map of both SD-JWTs to make sure they are different!
	 * We assume that the change for hash collisions is irrelevant here.
	 *
	 * @param sdjwt1 SD-JWT to check for consistency
	 * @param sdjwt2 SD-JWT to check for consitency
	 *
	 * @return [ConsistencyResult] showing if the two SD-JWTs are consistent according to the rules above.
	 */
	private fun checkDisclosureConsistency(sdjwt1: SdJwt, sdjwt2: SdJwt): ConsistencyResult {
		val visitedEntries = mutableSetOf<String>()
		for (entry in sdjwt1.innerJwt.disclosuresMap.entries) {
			if (visitedEntries.contains(entry.key)) {
				return ConsistencyResult.Error
			}
			visitedEntries.add(entry.key)
		}
		for (entry in sdjwt2.innerJwt.disclosuresMap.entries) {
			if (visitedEntries.contains(entry.key)) {
				return ConsistencyResult.Error
			}
			visitedEntries.add(entry.key)
		}
		return ConsistencyResult.Ok
	}

	/**
	 * Checks object consistency with the same rules as top level consistency checks
	 *
	 * @param left Object to compare
	 * @param right Object to compare
	 *
	 * @return [ConsistencyResult] showing if the two objects are consistent according to the rules above.
	 */
	private fun checkObjectConsistency(left: Value, right: Value): ConsistencyResult {
		for (entry in left.asObject() ?: return ConsistencyResult.Error) {
			val value2 = right[entry.key]
			if (mustBeDifferent.contains(entry.key)) {
				if (entry.value.isSame(value2)) {
					return ConsistencyResult.Error
				} else {
					continue
				}
			}
			if (skippedClaims.contains(entry.key)) {
				continue
			}
			if (entry.value.isObject()) {
				val result = checkObjectConsistency(entry.value, value2)
				when (result) {
					ConsistencyResult.Ok -> continue
					ConsistencyResult.Warn -> continue
					ConsistencyResult.Error -> return result
				}
			}
			if (entry.value.isArray()) {
				val result = checkArrayConsistency(entry.value, value2)
				when (result) {
					ConsistencyResult.Ok -> continue
					ConsistencyResult.Warn -> continue
					ConsistencyResult.Error -> return result
				}
			}
			if (!entry.value.isSame(value2)) {
				return ConsistencyResult.Error
			}
		}
		return ConsistencyResult.Ok
	}

	/**
	 * Checks array consistency by comparing the two arrays element by element.
	 * Nested objects and arrays are recursed into with the same rules as the
	 * object checks, so per-element meta claims (e.g. `_sd`) are skipped instead
	 * of being compared for exact (salt-dependent) equality.
	 *
	 * @param left Array to compare
	 * @param right Array to compare
	 *
	 * @return [ConsistencyResult] showing if the two arrays are consistent according to the rules above.
	 */
	private fun checkArrayConsistency(left: Value, right: Value): ConsistencyResult {
		val leftArray = left.asArray() ?: return ConsistencyResult.Error
		val rightArray = right.asArray() ?: return ConsistencyResult.Error
		if (leftArray.size != rightArray.size) {
			return ConsistencyResult.Error
		}
		for (i in leftArray.indices) {
			val leftElement = leftArray[i]
			val rightElement = rightArray[i]
			val elementConsistency = when {
				leftElement.isObject() -> checkObjectConsistency(leftElement, rightElement)
				leftElement.isArray() -> checkArrayConsistency(leftElement, rightElement)
				else -> if (leftElement.isSame(rightElement)) ConsistencyResult.Ok else ConsistencyResult.Error
			}
			when (elementConsistency) {
				ConsistencyResult.Ok -> {}
				ConsistencyResult.Warn -> {}
				ConsistencyResult.Error -> return elementConsistency
			}
		}
		return ConsistencyResult.Ok
	}
}