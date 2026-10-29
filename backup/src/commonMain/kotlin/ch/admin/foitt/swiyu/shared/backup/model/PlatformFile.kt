package ch.admin.foitt.swiyu.shared.backup.model

import kotlinx.io.files.Path

expect class PlatformFile {
    val path: Path
}

internal expect fun Path.toPlatformFile(): PlatformFile
