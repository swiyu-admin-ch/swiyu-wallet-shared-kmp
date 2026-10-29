package ch.admin.foitt.swiyu.shared.backup.util

import ch.admin.foitt.swiyu.shared.backup.model.BackupFile
import kotlinx.io.files.Path
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal actual fun writeZipArchive(archivePath: Path, files: List<BackupFile>) {
    ZipOutputStream(FileOutputStream(archivePath.toString())).use { zip ->
        for (file in files) {
            zip.putNextEntry(ZipEntry(file.fileName))
            try {
                zip.write(file.content.encodeToByteArray())
            } finally {
                zip.closeEntry()
            }
        }
    }
}
