package ch.admin.foitt.swiyu.shared.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class ContainerEncryptionContainerV1(
    @SerialName("salts")
    val salts: Map<String, String>,
) : Container {
    override val type = TYPE

    companion object {
        const val TYPE = "ContainerEncryptionContainerV1"
    }
}
