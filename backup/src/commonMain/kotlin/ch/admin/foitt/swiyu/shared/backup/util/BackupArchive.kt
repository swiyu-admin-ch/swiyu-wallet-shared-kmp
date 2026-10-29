package ch.admin.foitt.swiyu.shared.backup.util

import ch.admin.foitt.swiyu.shared.backup.model.BackupFile
import ch.admin.foitt.swiyu.shared.backup.model.PlatformFile
import ch.admin.foitt.swiyu.shared.backup.model.toPlatformFile
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(FormatStringsInDatetimeFormats::class)
private val backupDateFormat = DateTimeComponents.Format {
    byUnicodePattern("yyyy-MM-dd'T'HH-mm-ss")
}

@OptIn(ExperimentalTime::class)
@Suppress("TooGenericExceptionCaught")
internal fun writeBackupArchive(directory: String, files: List<BackupFile>): PlatformFile {
    val directoryPath = Path(directory)
    if (!SystemFileSystem.exists(directoryPath)) {
        SystemFileSystem.createDirectories(directoryPath)
    }

    val date = Clock.System.now().format(backupDateFormat)
    val fileName = "backup-$date.wbak"
    val archivePath = Path(directory, fileName)
    try {
        writeZipArchive(archivePath, files)
    } catch (failure: Throwable) {
        try {
            SystemFileSystem.delete(archivePath, mustExist = false)
        } catch (cleanupFailure: Throwable) {
            failure.addSuppressed(cleanupFailure)
        }
        throw failure
    }
    return SystemFileSystem.resolve(archivePath).toPlatformFile()
}

internal expect fun writeZipArchive(archivePath: Path, files: List<BackupFile>)
