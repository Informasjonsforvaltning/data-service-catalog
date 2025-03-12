package no.fdk.dataservicecatalog.unit.service

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.service.extract
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
                it.op == OpEnum.REPLACE && it.path == "/pages" && it.value == listOf("https://example.com/tos")
            })
        }
    }
}
