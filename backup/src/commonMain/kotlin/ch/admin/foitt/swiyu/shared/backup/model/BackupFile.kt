package ch.admin.foitt.swiyu.shared.backup.model

internal data class BackupFile(val name: String, val extension: Extension, val content: String) {
    val fileName = "$name.${extension.value}"

    enum class Extension(val value: String) {
        JSON("json"),
        JWE("jwe"),
    }
}
