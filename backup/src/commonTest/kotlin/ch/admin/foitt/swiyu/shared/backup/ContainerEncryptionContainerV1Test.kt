package ch.admin.foitt.swiyu.shared.backup

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerEncryptionContainerV1Test {

    private val json = Json {
        encodeDefaults = true
    }

    @Test
    fun `encryption container is correctly constructed`() {
        val salts = mapOf(
            "wbak-0.jwe" to "-zNe8UtdtuGWMD25pVZoPphywDGZeOqpso8f3Z5q7VE",
            "wbak-1.jwe" to "JGKJ6lhODH0bpgGhGzkopHA-unxJulRe5UqN02HsLMM",
        )

        val encryptionContainer = ContainerEncryptionContainerV1(salts)
        val content = json.encodeToString(encryptionContainer)
        val container = json.parseToJsonElement(content).jsonObject
    
        assertEquals(ContainerEncryptionContainerV1.TYPE, container.getValue("type").jsonPrimitive.content)
        assertEquals(salts, container.getValue("salts").jsonObject.mapValues { it.value.jsonPrimitive.content })
    }
}
