package ch.admin.foitt.swiyu.shared.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
internal data class BackupContainerV1(
    @SerialName("type")
    val type: String = TYPE,
    @OptIn(ExperimentalTime::class)
    @SerialName("creation_date")
    val creationDate: String,
) {
    companion object {
        const val TYPE = "WalletBackupContainerV1"

        @OptIn(ExperimentalTime::class)
        operator fun invoke(clock: Clock = Clock.System): BackupContainerV1 =
            BackupContainerV1(creationDate = clock.now().toString())
    }
}
