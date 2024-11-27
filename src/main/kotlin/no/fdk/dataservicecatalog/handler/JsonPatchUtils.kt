package no.fdk.dataservicecatalog.handler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.fdk.dataservicecatalog.domain.JsonPatchOperation
import no.fdk.dataservicecatalog.exception.BadRequestException
import no.fdk.dataservicecatalog.exception.InternalServerErrorException
import java.io.StringReader

inline fun <reified T> patchOriginal(original: T, operations: List<JsonPatchOperation>): T {
    validateOperations(operations)

    try {
        return applyPatch(original, operations)
    } catch (ex: Exception) {
        when (ex) {
            is JsonException,
            is JsonProcessingException,
            is IllegalArgumentException -> throw BadRequestException(ex.message)

            else -> throw InternalServerErrorException(ex.message)
        }
    }
}

inline fun <reified T> applyPatch(
    originalObject: T,
    operations: List<JsonPatchOperation>
): T {
    if (operations.isEmpty()) {
        return originalObject
    }

    with(jacksonObjectMapper().registerModules(JavaTimeModule())) {
        val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
        val original = Json.createReader(StringReader(writeValueAsString(originalObject))).readObject()

        return Json.createPatch(changes).apply(original)
            .let { readValue(it.toString()) }
    }
}

fun validateOperations(operations: List<JsonPatchOperation>) {
    val invalidPaths = listOf("/id", "/created", "/modified", "/version", "/catalogId")

    if (operations.any { it.path in invalidPaths }) {
        throw BadRequestException("Patch of paths $invalidPaths is not permitted")
    }
}
