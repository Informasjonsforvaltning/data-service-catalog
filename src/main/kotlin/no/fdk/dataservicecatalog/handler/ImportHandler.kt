package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.domain.allExtractionRecords
import no.fdk.dataservicecatalog.domain.hasError
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.service.ImportOpenApiService
import no.fdk.dataservicecatalog.service.ImportResultService
import no.fdk.dataservicecatalog.service.ImportService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImportHandler(
    private val importService: ImportService,
    private val importResultService: ImportResultService,
    private val importOpenApiService: ImportOpenApiService
) {
    @Transactional
    fun importOpenApi(catalogId: String, specification: String): ImportResult {
        val parseResult = importOpenApiService.parse(specification)

        val openAPI = parseResult.openAPI
            ?: throw OpenApiParseException("Unexpected error parsing OpenAPI import")

        val dataServiceExtractions = openAPI.servers.map { server ->
            val externalId = server.url

            val dataService = importResultService.findDataServiceIdByCatalogIdAndExternalId(catalogId, externalId)
                ?.let { importService.findDataService(it) }
                ?: importService.createDataService(externalId, catalogId)

            importOpenApiService.extract(openAPI, dataService)
        }

        return if (dataServiceExtractions.isEmpty() || dataServiceExtractions.hasError) {
            importResultService.save(catalogId, dataServiceExtractions.allExtractionRecords, ImportResultStatus.FAILED)
                .also { logger.warn("Errors occurred during OpenAPI import for catalog $catalogId") }
        } else {
            dataServiceExtractions.forEach { extraction ->
                importService.save(extraction.dataService)
                    .also { logger.info("Updated data service (${it.id}) in catalog: $catalogId") }
            }

            importResultService.save(
                catalogId,
                dataServiceExtractions.allExtractionRecords,
                ImportResultStatus.COMPLETED
            ).also {
                logger.info("OpenAPI import completed successfully for catalog $catalogId")
            }
        }
    }

    fun getResults(catalogId: String): List<ImportResult> {
        return importResultService.getResults(catalogId)
    }

    fun getResult(statusId: String): ImportResult? {
        return importResultService.getResult(statusId)
    }

    fun deleteResult(catalogId: String, resultId: String) {
        val result = importResultService.getResult(resultId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Import result with id: $resultId not found in Catalog with id: $catalogId")

        importResultService.deleteResult(result)

        logger.info("Deleted import result with id: $resultId in Catalog with id: $catalogId")
    }
}

private val logger: Logger = LoggerFactory.getLogger(ImportHandler::class.java)
