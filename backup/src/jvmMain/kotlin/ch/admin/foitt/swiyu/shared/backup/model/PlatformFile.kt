package ch.admin.foitt.swiyu.shared.backup.model

import kotlinx.io.files.Path
import java.io.File

actual class PlatformFile(file: File) {
    actual val path: Path = Path(file.path)
}

actual fun Path.toPlatformFile(): PlatformFile = PlatformFile(File(this.toString()))