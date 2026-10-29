package ch.admin.foitt.swiyu.shared.backup.util

import ch.admin.foitt.swiyu.shared.backup.model.BackupFile
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString

internal actual fun writeZipArchive(archivePath: Path, files: List<BackupFile>) {
    Zip.open(
        path = archivePath,
        mode = Zip.Mode.Write,
    ).use { zip ->
        files.forEach { file ->
            val content = Buffer().apply { writeString(file.content) }
            zip.entryFromSource(Path(file.fileName), content)
        }
    }
}
