package no.fdk.dataservicecatalog.service

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.core.models.SwaggerParseResult
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.handler.createPatchOperations
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URISyntaxException

@Service
class ImportOpenApiService {

    fun parse(specification: String): SwaggerParseResult {
        return OpenAPIParser().readContents(specification, null, null)
    }

    fun extract(openAPI: OpenAPI, originalDataService: DataService): DataServiceExtraction {
        return openAPI.extract(originalDataService)
    }
}

private val EMAIL_REGEX = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

fun OpenAPI.extract(originalDataService: DataService): DataServiceExtraction {
    val title = extractTitle()
    val description = extractDescription()
    val contactPoint = extractContactPoint()
    val pages = extractPages()
    val landingPage = extractLandingPage()

    val updatedDataService = originalDataService.copy(
        title = title.first,
        description = description.first,
        contactPoint = contactPoint.first,
        pages = pages.first,
        landingPage = landingPage.first
    )

    val issues = listOf(
        title.second,
        description.second,
        contactPoint.second,
        pages.second,
        landingPage.second
    ).flatten()

    val operations = createPatchOperations(originalDataService, updatedDataService)

    val extractResult = ExtractResult(operations, issues)

    val extractionRecord = ExtractionRecord(
        externalId = updatedDataService.endpointUrl,
        internalId = updatedDataService.id,
        extractResult = extractResult,
    )

    return DataServiceExtraction(updatedDataService, extractionRecord)
}

private fun OpenAPI.extractTitle(): Pair<LocalizedStrings, List<Issue>> {
    val issues = mutableListOf<Issue>()

    val title = info?.title

    when {
        title == null -> {
            issues.add(Issue(IssueType.ERROR, "attribute info.title is missing"))
        }

        title.isBlank() -> {
            issues.add(Issue(IssueType.ERROR, "attribute info.title is blank"))
        }
    }

    return LocalizedStrings(en = title?.takeIf { it.isNotBlank() }) to issues
}

private fun OpenAPI.extractDescription(): Pair<LocalizedStrings?, List<Issue>> {
    val description = info?.description?.takeIf { it.isNotBlank() }
        ?: info?.summary?.takeIf { it.isNotBlank() }

    return description?.let { LocalizedStrings(en = it) to emptyList() }
        ?: (null to emptyList())
}

private fun OpenAPI.extractContactPoint(): Pair<ContactPoint?, List<Issue>> {
    val issues = mutableListOf<Issue>()

    val name = info?.contact?.name
        ?.takeIf { it.isNotBlank() }
        ?.let {
            LocalizedStrings(en = it)
        }

    val email = info?.contact?.email
        ?.takeIf { it.isNotBlank() }
        ?.let {
            if (!EMAIL_REGEX.matches(it)) {
                issues.add(Issue(IssueType.WARNING, "attribute contact.email has invalid format: $it"))
                null
            } else {
                it
            }
        }

    val url = info?.contact?.url
        ?.takeIf {
            if (it.isBlank()) {
                false
            } else if (!it.isValidUri()) {
                issues.add(Issue(IssueType.WARNING, "attribute contact.url has invalid format: $it"))
                false
            } else {
                true
            }
        }

    val contactPoint = if (name != null || email != null || url != null) {
        ContactPoint(name = name, email = email, url = url)
    } else {
        null
    }

    return contactPoint to issues
}

private fun OpenAPI.extractPages(): Pair<List<String>?, List<Issue>> {
    val issues = mutableListOf<Issue>()

    val pages = info?.termsOfService
        ?.takeIf {
            if (it.isBlank()) {
                false
            } else if (!it.isValidUri()) {
                issues.add(Issue(IssueType.WARNING, "attribute info.termsOfService has invalid format: $it"))
                false
            } else {
                true
            }
        }
        ?.let { listOf(it) }

    return pages to issues
}

private fun OpenAPI.extractLandingPage(): Pair<String?, List<Issue>> {
    val issues = mutableListOf<Issue>()

    val landingPage = externalDocs?.url
        ?.takeIf {
            if (it.isBlank()) {
                false
            } else if (!it.isValidUri()) {
                issues.add(Issue(IssueType.WARNING, "attribute externalDocs.url has invalid format: $it"))
                false
            } else {
                true
            }
        }

    return landingPage to issues
}

private fun String.isValidUri(): Boolean {
    try {
        val uri = URI(this)

        if (!uri.isAbsolute || uri.scheme == null) {
            return false
        }
    } catch (e: URISyntaxException) {
        return false
    }

    return true
}


