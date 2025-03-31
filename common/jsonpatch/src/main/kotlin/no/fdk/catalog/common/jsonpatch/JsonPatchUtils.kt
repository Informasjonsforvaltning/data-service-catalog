package no.fdk.catalog.common.jsonpatch

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import java.io.StringReader

inline fun <reified T> patchOriginal(original: T, operations: List<JsonPatchOperation>): T {
    validateOperations(operations)

    try {
        return applyPatch(original, operations)
    } catch (ex: Exception) {
        when (ex) {
            is JsonException,
            is JsonProcessingException,
            is IllegalArgumentException -> throw RuntimeException(ex.message)

            else -> throw RuntimeException(ex.message)
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

    with(jacksonObjectMapper().registerModule(JavaTimeModule())) {
        val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
        val original = Json.createReader(StringReader(writeValueAsString(originalObject))).readObject()

        return Json.createPatch(changes).apply(original)
            .let { readValue(it.toString()) }
    }
}

fun validateOperations(operations: List<JsonPatchOperation>) {
    val invalidPaths =
        listOf("/id", "/catalogId", "/created", "/modified", "/modifiedBy", "/version", "/published", "/publishedDate")

    if (operations.any { it.path in invalidPaths }) {
        throw RuntimeException("Patch of paths $invalidPaths is not permitted")
    }
}

inline fun <reified T> createPatchOperations(originalObject: T, updatedObject: T): List<JsonPatchOperation> =
    with(jacksonObjectMapper().registerModule(JavaTimeModule())) {
        val original = Json.createReader(StringReader(writeValueAsString(originalObject))).readObject()
        val updated = Json.createReader(StringReader(writeValueAsString(updatedObject))).readObject()

        return readValue(Json.createDiff(original, updated).toString())
    }
