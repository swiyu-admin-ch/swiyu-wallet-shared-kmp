package ch.admin.foitt.swiyu.shared.dcql.model

import uniffi.kapun_credential_core_rust.PointerPart
import uniffi.kapun_util_rust.Value

/**
 * Resolved claim value for a DCQL request.
 *
 * @property paths list of dcql pointer paths.
 * @property value Resolved claim value.
 */
data class DcqlClaimValue(val paths: List<PointerPart>, val value: Value)
