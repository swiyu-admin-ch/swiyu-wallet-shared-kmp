package ch.admin.foitt.swiyu.shared.backup.util

import kotlin.io.encoding.Base64

internal val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

internal inline fun <T, R : Any> List<T>.ifNotEmpty(transform: (List<T>) -> R): R? =
    takeIf { it.isNotEmpty() }?.let(transform)
