package no.fdk.catalog.dataservice.importer.unit

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import no.fdk.catalog.common.jsonpatch.OpEnum
import no.fdk.catalog.dataservice.core.domain.ContactPoint
import no.fdk.catalog.dataservice.core.domain.DataService
import no.fdk.catalog.dataservice.core.domain.LocalizedStringLists
import no.fdk.catalog.dataservice.core.domain.LocalizedStrings
import no.fdk.catalog.dataservice.importer.domain.IssueType
import no.fdk.catalog.dataservice.importer.service.extract
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class ImportOpenApiServiceTest {

    @Test
    fun `should extract title`() {
        val info = Info()
        info.title = "title"

        val openAPI = OpenAPI()
        openAPI.info = info

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals(
            LocalizedStrings(nb = null, nn = null, en = "title"),
            dataServiceExtraction.dataService.title
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(1, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/title/en" && it.value == "title"
            })

            assertTrue(result.issues.isEmpty())
        }
    }

    @Test
    fun `should fail to extract title`() {
        val info = Info()
        info.title = ""

        val openAPI = OpenAPI()
        openAPI.info = info

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(1, result.issues.size)

            assertTrue(result.issues.any {
                it.type == IssueType.ERROR && it.message.contains("info.title")
            })
        }
    }

    @Test
    fun `should extract description`() {
        val info = Info()
        info.title = "title"
        info.description = "description"

        val openAPI = OpenAPI()
        openAPI.info = info

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals(
            LocalizedStrings(nb = null, nn = null, en = "description"),
            dataServiceExtraction.dataService.description!!
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(2, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/description" && it.value == mapOf(
                    "nb" to null,
                    "nn" to null,
                    "en" to "description"
                )
            })

            assertTrue(result.issues.isEmpty())
        }
    }

    @Test
    fun `should extract contact point`() {
        val contact = Contact()
        contact.name = "name"
        contact.email = "invalid"
        contact.url = "https://example.com/contact"

        val info = Info()
        info.title = "title"
        info.contact = contact

        val openAPI = OpenAPI()
        openAPI.info = info

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals(
            ContactPoint(
                name = LocalizedStrings(nb = null, nn = null, en = "name"),
                url = "https://example.com/contact"
            ),
            dataServiceExtraction.dataService.contactPoint!!
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(2, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/contactPoint" && it.value == mapOf(
                    "name" to mapOf(
                        "nb" to null,
                        "nn" to null,
                        "en" to "name"
                    ),
                    "phone" to null,
                    "email" to null,
                    "url" to "https://example.com/contact"
                )
            })

            assertEquals(1, result.issues.size)

            assertTrue(result.issues.any {
                it.type == IssueType.WARNING && it.message.contains("contact.email")
            })
        }
    }

    @Test
    fun `should extract pages`() {
        val info = Info()
        info.title = "title"
        info.termsOfService = "https://example.com/tos"

        val openAPI = OpenAPI()
        openAPI.info = info

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals(listOf("https://example.com/tos"), dataServiceExtraction.dataService.pages!!)

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(2, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/pages" && it.value == listOf(
                    "https://example.com/tos"
                )
            })
        }
    }

    @Test
    fun `should extract landing page`() {
        val info = Info()
        info.title = "title"

        val externalDocumentation = ExternalDocumentation()
        externalDocumentation.url = "https://example.com/doc"

        val openAPI = OpenAPI()
        openAPI.info = info
        openAPI.externalDocs = externalDocumentation

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals("https://example.com/doc", dataServiceExtraction.dataService.landingPage!!)

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(2, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/landingPage" && it.value == "https://example.com/doc"
            })
        }
    }

    @Test
    fun `should extract keywords`() {
        val info = Info()
        info.title = "title"

        val tag = io.swagger.v3.oas.models.tags.Tag()
        tag.name = "tag"

        val openAPI = OpenAPI()
        openAPI.info = info
        openAPI.tags = listOf(tag)

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = openAPI.extract(dataService)

        assertEquals(LocalizedStringLists(en = listOf("tag")), dataServiceExtraction.dataService.keywords!!)

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(2, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/keywords" && it.value == mapOf(
                    "nb" to null,
                    "nn" to null,
                    "en" to listOf("tag")
                )
            })
        }
    }
}
