package ch.admin.foitt.swiyu.shared.backup

import ch.admin.foitt.swiyu.shared.backup.util.base64Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class BackupEncryptionTest {

    private val recoveryKey = ByteArray(32) { it.toByte() }
    private val salt = ByteArray(32) { (it + 32).toByte() }
    private val plaintext = """{"type":"VerifiableCredentialContainerV1","vcs":[]}"""

    @Test
    fun `backup encryption correctly creates a compact JWE`() {
        val jwe = BackupEncryption.encrypt(plaintext, recoveryKey, salt)
        val parts = jwe.split('.')
        assertEquals(5, parts.size)

        val header = Json.parseToJsonElement(base64Url.decode(parts[0]).decodeToString()).jsonObject
        assertEquals("dir", header.getValue("alg").jsonPrimitive.content)
        assertEquals("A256GCM", header.getValue("enc").jsonPrimitive.content)
        assertEquals("application/json", header.getValue("cty").jsonPrimitive.content)
        assertEquals("", parts[1])
        assertEquals(12, base64Url.decode(parts[2]).size)
        assertEquals(16, base64Url.decode(parts[4]).size)
        assertFalse(jwe.contains('='))
    }

    @Test
    fun `backup encryption creates ciphertext`() {
        val contents = listOf(
            "a".repeat(1),
            "a".repeat(10),
            "a".repeat(100)
        )

        contents.forEach { content ->
            val plaintextBytes = content.encodeToByteArray()

            val jwe = BackupEncryption.encrypt(content, recoveryKey, salt)
            val ciphertext = base64Url.decode(jwe.split('.')[3])
            assertEquals(plaintextBytes.size, ciphertext.size)
        }
    }

    @Test
    fun `backup encryption correctly derives a key using HKDF SHA256 with empty info`() {
        val expectedKey = "9d7454efd23c4734540dbc9ab88dac56d22ef56757ac77b92cbb857ec32d1f8e".hexToByteArray()

        val derivedKey = BackupEncryption.deriveKey(recoveryKey, salt)
        assertContentEquals(expectedKey, derivedKey)
    }

    @Test
    fun `backup encryption uses different IVs`() {
        val jwe1 = BackupEncryption.encrypt(plaintext, recoveryKey, salt)
        val jwe2 = BackupEncryption.encrypt(plaintext, recoveryKey, salt)

        assertNotEquals(jwe1.split('.')[2], jwe2.split('.')[2])
    }

}
