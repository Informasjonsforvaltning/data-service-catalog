package no.fdk.dataservicecatalog.handler

import io.swagger.parser.OpenAPIParser
import no.fdk.dataservicecatalog.domain.ExtractionRecord
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import no.fdk.dataservicecatalog.repository.ImportResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*

@Service
class ImportHandler(
    private val dataServiceRepository: DataServiceRepository,
    private val importResultRepository: ImportResultRepository
) {

    fun importOpenApi(catalogId: String, dataService: String): ImportResult {
        val parseResult = OpenAPIParser().readContents(dataService, null, null)

        val openAPI = parseResult.openAPI

        if (parseResult.messages.isNotEmpty() || openAPI == null) {
            parseResult.messages.forEach(System.out::println)

            logger.error("Error parsing OpenAPI import...")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "...")
        }

        return ImportResult(
            id = UUID.randomUUID().toString(),
            created = LocalDateTime.now(),
            catalogId = catalogId,
            status = ImportResultStatus.COMPLETED
        )
    }

    fun getResults(catalogId: String): List<ImportResult> {
        return importResultRepository.findAllByCatalogId(catalogId);
    }

    fun getResult(statusId: String): ImportResult? {
        return importResultRepository.findByIdOrNull(statusId)
    }

    private fun saveImportResult(
        catalogId: String, extractionRecords: List<ExtractionRecord>, status: ImportResultStatus
    ): ImportResult {
        return importResultRepository.save(
            ImportResult(
                id = UUID.randomUUID().toString(),
                created = LocalDateTime.now(),
                catalogId = catalogId,
                status = status,
                extractionRecords = extractionRecords
            )
        )
    }
}

private val logger: Logger = LoggerFactory.getLogger(ImportHandler::class.java)
