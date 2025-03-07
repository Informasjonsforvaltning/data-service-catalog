package no.fdk.dataservicecatalog.service

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.core.models.SwaggerParseResult
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.handler.createPatchOperations
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URISyntaxException
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

private val EMAIL_REGEX = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

fun SwaggerParseResult.extract(originalDataService: DataService): DataServiceExtraction {
    val contactPoint = openAPI.extractContactPoint()

    val updatedDataService = originalDataService.copy(
        endpointUrl = openAPI.servers.first().url,
        title = LocalizedStrings(en = openAPI?.info?.title),
        contactPoint = contactPoint.first
    )

    val issues = mutableListOf<Issue>()

    messages
        .filter { !it.contains("paths") }
        .forEach {
            issues.add(Issue(IssueType.ERROR, it))
        }

    issues.addAll(
        listOf(
            contactPoint.second
        ).flatten()
    )

    val operations = createPatchOperations(originalDataService, updatedDataService)

    val extractResult = ExtractResult(operations, issues)

    val extractionRecord = ExtractionRecord(
        externalId = updatedDataService.endpointUrl,
        internalId = updatedDataService.id,
        extractResult = extractResult,
    )

    return DataServiceExtraction(updatedDataService, extractionRecord)
}

private fun OpenAPI.extractContactPoint(): Pair<ContactPoint?, List<Issue>> {
    val issues = mutableListOf<Issue>()

    val name = info?.contact?.name
        ?.takeIf { it.isNotBlank() }
        ?.let {
            LocalizedStrings(en = it)
        }

    val email = info?.contact?.email?.let {
        if (!EMAIL_REGEX.matches(it)) {
            issues.add(Issue(IssueType.WARNING, "attribute contact.email has invalid format: $it"))
            null
        } else {
            it
        }
    }

    val url = info?.contact?.url?.let {
        try {
            val uri = URI(it)

            if (!uri.isAbsolute || uri.scheme == null) {
                issues.add(Issue(IssueType.WARNING, "attribute contact.url has invalid format: $it"))
                null
            } else {
                it
            }
        } catch (e: URISyntaxException) {
            issues.add(Issue(IssueType.WARNING, "attribute contact.url has invalid format: $it"))
            null
        }
    }

    val contactPoint = if (name != null || email != null || url != null) {
        ContactPoint(name = name, email = email, url = url)
    } else {
        null
    }

    return contactPoint to issues
}
