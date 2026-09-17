package ch.admin.foitt.swiyu.shared.proximity

import ch.ubique.heidi.proximity.ProximityError

/**
 * State representation for proximity presentation that can be easily consumed by iOS
 */
sealed class ProximityState {
    object Initial : ProximityState()
    data class ReadyForEngagement(val qrCodeData: String) : ProximityState()
    data class Connecting(val verifierName: String) : ProximityState()
    data class Connected(val verifierName: String) : ProximityState()
    data class RequestingDocuments(
        val raw: String,
        val origin: String? = null,
    ) : ProximityState()
    data class SubmittingDocuments(val progress: Double? = null) : ProximityState()
    object PresentationCompleted : ProximityState()
    data class Error(val error: ProximityError) : ProximityState()
    object Disconnected : ProximityState()
}
