package no.fdk.dataservicecatalog.handler

import io.swagger.parser.OpenAPIParser
import no.fdk.dataservicecatalog.domain.ExtractionRecord
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.exception.BadRequestException
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import no.fdk.dataservicecatalog.repository.ImportResultRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ExceptionHandler
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

        if (parseResult.messages.isNotEmpty()) {
            throw OpenApiParseException(parseResult.messages)
        }

        if (openAPI == null) {
            throw OpenApiParseException(messages = null)
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
