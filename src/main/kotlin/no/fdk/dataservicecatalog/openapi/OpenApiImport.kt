package no.fdk.dataservicecatalog.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.handler.createPatchOperations
import java.util.*

fun OpenAPI.extract(objectMapper: ObjectMapper): DataServiceExtraction {
    val id = UUID.randomUUID().toString()

    val newDataService = ImportDataService(id)

    val updatedDataService = newDataService.copy()

    val issues = emptyList<Issue>()

    val operations = createPatchOperations(newDataService, updatedDataService, objectMapper)

    val extractResult = ExtractResult(operations, issues)

    val extractionRecord = ExtractionRecord(
        externalId = updatedDataService.id,
        internalId = updatedDataService.id,
        extractResult = extractResult,
    )

    return DataServiceExtraction(updatedDataService, extractionRecord)
}
