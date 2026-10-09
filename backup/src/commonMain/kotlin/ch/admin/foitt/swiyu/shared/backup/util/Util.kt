package ch.admin.foitt.swiyu.shared.backup.util

import ch.admin.foitt.swiyu.shared.backup.model.PlatformFile
import ch.admin.foitt.swiyu.shared.backup.model.toPlatformFile
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

internal fun writeFile(directory: String, fileName: String, content: String): PlatformFile {
    val path = Path(directory)
    if (!SystemFileSystem.exists(path)) {
        SystemFileSystem.createDirectories(path)
    }

    val filePath = Path(directory, fileName)

    SystemFileSystem.sink(filePath).buffered().use { sink ->
        sink.writeString(content)
    }

    return filePath.toPlatformFile()
}

internal inline fun <T, R : Any> List<T>.ifNotEmpty(transform: (List<T>) -> R): R? =
    takeIf { it.isNotEmpty() }?.let(transform)