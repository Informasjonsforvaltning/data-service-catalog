package no.fdk.dataservicecatalog.service

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.SwaggerParseResult
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.handler.createPatchOperations
import org.springframework.stereotype.Service
import java.util.*

@Service
class ImportOpenApiService {

    fun parse(specification: String): SwaggerParseResult {
        return OpenAPIParser().readContents(specification, null, null)
    }

    fun extract(parseResult: SwaggerParseResult, originalDataService: DataService): ImportResult {
        val dataServiceExtraction = parseResult.extract(originalDataService)

        return createImportResult(
            originalDataService.catalogId,
            dataServiceExtraction.extractionRecord,
            if (dataServiceExtraction.hasError) ImportResultStatus.FAILED else ImportResultStatus.COMPLETED
        )
    }

    private fun createImportResult(
        catalogId: String,
        extractionRecord: ExtractionRecord,
        status: ImportResultStatus
    ): ImportResult {
        return ImportResult(
            id = UUID.randomUUID().toString(),
            catalogId = catalogId,
            status = status,
            extractionRecords = listOf(extractionRecord)
        )
    }
}

fun SwaggerParseResult.extract(originalDataService: DataService): DataServiceExtraction {
    val updatedDataService = originalDataService.copy(
        endpointUrl = openAPI.servers.first().url,
        title = LocalizedStrings(en = openAPI?.info?.title)
    )

    val issues = mutableListOf<Issue>()

    messages
        .filter { !it.contains("paths") }
        .forEach {
            issues.add(Issue(IssueType.ERROR, it))
        }

    val operations = createPatchOperations(originalDataService, updatedDataService)

    val extractResult = ExtractResult(operations, issues)

    val extractionRecord = ExtractionRecord(
        externalId = updatedDataService.endpointUrl,
        internalId = updatedDataService.id,
        extractResult = extractResult,
    )

    return DataServiceExtraction(updatedDataService, extractionRecord)
}
