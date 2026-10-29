package ch.admin.foitt.swiyu.shared.backup.model

import kotlinx.io.files.Path
import platform.Foundation.NSURL

actual class PlatformFile(val url: NSURL) {
    actual val path: Path = Path(url.path ?: "")
}

actual fun Path.toPlatformFile(): PlatformFile = PlatformFile(NSURL.fileURLWithPath(this.toString()))
