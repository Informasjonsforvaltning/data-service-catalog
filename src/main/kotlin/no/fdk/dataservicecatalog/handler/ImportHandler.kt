package no.fdk.dataservicecatalog.handler

import io.swagger.v3.parser.core.models.SwaggerParseResult
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.domain.allOperations
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.service.ImportOpenApiService
import no.fdk.dataservicecatalog.service.ImportResultService
import no.fdk.dataservicecatalog.service.ImportService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ImportHandler(
    private val importService: ImportService,
    private val importResultService: ImportResultService,
    private val importOpenApiService: ImportOpenApiService
) {
    fun importOpenApi(catalogId: String, specification: String): ImportResult {
        val parseResult: SwaggerParseResult

        try {
            parseResult = importOpenApiService.parse(specification)
        } catch (ex: Exception) {
            logger.error("Unexpected error during OpenAPI import for catalog: $catalogId", ex)
            throw OpenApiParseException("Unexpected error during OpenAPI import")
        }

        val openAPI = parseResult.openAPI ?: throw OpenApiParseException("Error parsing OpenAPI import")

        if (openAPI.servers.size > 1) {
            throw OpenApiParseException("Attribute servers must be exactly one")
        }

        val externalId = parseResult.openAPI.servers.first().url

        val dataService = importResultService.findDataServiceIdByExternalId(externalId)
            ?.let { importService.findDataService(it) }
            ?: importService.createDataService(externalId, catalogId)

        val importResult = importOpenApiService.extract(parseResult, dataService)

        return if (importResult.status == ImportResultStatus.FAILED) {
            importResultService.save(importResult)
        } else {
            importResult.extractionRecords.firstOrNull()
                ?.let { applyPatch(dataService, it.allOperations) }
                ?.let { importService.save(it) }
                ?.also { logger.info("Updated data service (${it.id}) in catalog: $catalogId") }

            importResultService.save(importResult)
        }
    }

    fun getResults(catalogId: String): List<ImportResult> {
        return importResultService.getResults(catalogId);
    }

    fun getResult(statusId: String): ImportResult? {
        return importResultService.getResult(statusId)
    }
}

private val logger: Logger = LoggerFactory.getLogger(ImportHandler::class.java)
