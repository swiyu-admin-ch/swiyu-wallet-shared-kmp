package ch.admin.foitt.swiyu.shared.proximity

import ch.ubique.heidi.proximity.ProximityProtocol
import ch.ubique.heidi.proximity.documents.DocumentRequest
import ch.ubique.heidi.proximity.wallet.ProximityWallet
import ch.ubique.heidi.proximity.wallet.ProximityWalletState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * Controller for managing proximity presentation between iOS app and heidi SDK.
 */
class ProximityPresentationController {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	private var engagementJob: Job? = null
	private lateinit var wallet: ProximityWallet

	private val _state = MutableStateFlow<ProximityState>(ProximityState.Initial)
	val state: StateFlow<ProximityState> = _state.asStateFlow()

	/**
	 * Start a reverse proximity engagement using the transport protocol defined in ISO 18013-5.
	 * This generates a QR code that the verifier can scan.
	 * @return The engagement data (including QR code data)
	 */
	fun startEngagement() {
		return startEngagementInternal(readerEngagement = null)
	}

	/**
	 * Start a reverse proximity engagement using the transport protocol defined in ISO 18013-5.
	 * This uses a reader engagement QR code provided by the verifier.
	 * @return The engagement data (contains the provided reader engagement)
	 */
	fun startEngagementReverse(readerEngagement: String) {
		val normalizedReaderEngagement = normalizeReaderEngagement(readerEngagement)
		return startEngagementInternal(readerEngagement = normalizedReaderEngagement)
	}

	@OptIn(ExperimentalUuidApi::class)
	private fun startEngagementInternal(readerEngagement: String?) {
		val isReverse = !readerEngagement.isNullOrBlank()
		val serviceUuid = Uuid.random()
		val peripheralServerUuid = Uuid.random()

		wallet = if (isReverse) {
			ProximityWallet.createReverse(
				protocol = ProximityProtocol.MDL, // iso18013-5
				scope = scope,
				readerEngagement = readerEngagement
			)
		} else {
			ProximityWallet.create(
				protocol = ProximityProtocol.MDL,
				scope = scope,
				serviceUuid = serviceUuid,
				peripheralServerUuid = peripheralServerUuid
			)
		}

		engagementJob?.cancel()
		engagementJob = scope.launch {
			wallet.startEngagement("")
			startCollectingWalletState()
		}
	}

	private fun normalizeReaderEngagement(readerEngagement: String): String {
		val trimmed = readerEngagement.trim()
		return when {
			trimmed.startsWith("mdoc:") -> trimmed.removePrefix("mdoc:")
			else -> trimmed
		}
	}

	/**
	 * Reset and disconnect the wallet
	 */
	fun reset() {
		if (::wallet.isInitialized) {
			wallet.disconnect()
		}
		_state.value = ProximityState.Disconnected
	}

	/**
	 * Decline the ongoing proximity session and disconnect.
	 */
	fun decline() {
		if (::wallet.isInitialized) {
			wallet.declinePresentation()
		} else {
			reset()
		}
		_state.value = ProximityState.Disconnected
	}

	/**
	 * Cleanup resources
	 */
	fun dispose() {
		reset()
		scope.cancel()
	}

	fun submitDocument(data: ByteArray) {
		wallet.submitDocument(data)
	}

	private fun startCollectingWalletState() {
		scope.launch {
			wallet.walletState.collect { walletState ->
				when (walletState) {
					is ProximityWalletState.Initial -> {
						_state.value = ProximityState.Initial
					}
					is ProximityWalletState.ReadyForEngagement -> {
						_state.value = ProximityState.ReadyForEngagement(walletState.qrCodeData)
					}
					is ProximityWalletState.Connecting -> {
						_state.value = ProximityState.Connecting(walletState.verifierName)
					}
					is ProximityWalletState.Connected -> {
						_state.value = ProximityState.Connected(walletState.verifierName)
					}
					is ProximityWalletState.RequestingDocuments -> {
						handleDocumentRequest(walletState.request)
					}
					is ProximityWalletState.SubmittingDocuments -> {
						_state.value = ProximityState.SubmittingDocuments(walletState.progress)
					}
					is ProximityWalletState.PresentationCompleted -> {
						_state.value = ProximityState.PresentationCompleted
					}
					is ProximityWalletState.Disconnected -> {
						_state.value = ProximityState.Disconnected
					}
					is ProximityWalletState.Error -> {
						_state.value = ProximityState.Error(walletState.error)
					}
				}
			}
		}
	}

	private fun handleDocumentRequest(request: DocumentRequest) {
		when (request) {
			is DocumentRequest.OpenId4Vp -> {
				_state.value = ProximityState.RequestingDocuments(
					raw = request.parJwt,
					origin = request.origin,
				)
			}
			is DocumentRequest.Mdl -> {}
		}
	}
}