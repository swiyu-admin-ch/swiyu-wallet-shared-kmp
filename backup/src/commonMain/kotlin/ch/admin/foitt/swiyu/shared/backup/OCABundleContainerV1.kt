package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.dto.BundleDto
import ch.admin.foitt.swiyu.shared.backup.dto.toBundleDtoList
import ch.admin.foitt.swiyu.shared.backup.model.VC
import ch.admin.foitt.swiyu.shared.backup.util.ifNotEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
internal class OCABundleContainerV1 private constructor(
    @SerialName("bundles")
    val bundles: List<BundleDto>,
) : Container {
    override val type = TYPE

    companion object {
        const val TYPE = "OCABundleContainerV1"

        fun from(verifiableCredentials: List<VC>): OCABundleContainerV1? = verifiableCredentials
            .toBundleDtoList()
            .ifNotEmpty { bundleDtos -> OCABundleContainerV1(bundleDtos) }
    }
}



