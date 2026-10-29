package ch.admin.foitt.swiyu.shared.backup

import kotlinx.serialization.SerialName

internal interface Container {
    @SerialName("type")
    val type: String
}
