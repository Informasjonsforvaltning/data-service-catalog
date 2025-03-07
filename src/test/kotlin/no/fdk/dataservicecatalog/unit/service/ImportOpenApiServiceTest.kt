package no.fdk.dataservicecatalog.unit.service

import io.swagger.parser.OpenAPIParser
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.service.extract
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImportOpenApiServiceTest {

    @Test
    fun `should extract title`() {
        val specification = """
            {
                "info": {
                    "version": "1.0",
                    "title": "title",
                    "description": "description"
                },
                "servers": [
                    {
                        "url": "https://example.com"
                    }
                ],
                "paths": {}
            }
        """.trimIndent()

        val parseResult = OpenAPIParser().readContents(specification, null, null)

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = parseResult.extract(dataService)

        assertEquals(
            LocalizedStrings(nb = null, nn = null, en = "title"),
            dataServiceExtraction.dataService.title
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(3, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/title/en" && it.value == "title"
            })

            assertTrue(result.issues.isEmpty())
        }
    }

    @Test
    fun `should extract description`() {
        val specification = """
            {
                "info": {
                    "version": "1.0",
                    "title": "title",
                    "description": "description"
                },
                "servers": [
                    {
                        "url": "https://example.com"
                    }
                ],
                "paths": {}
            }
        """.trimIndent()

        val parseResult = OpenAPIParser().readContents(specification, null, null)

        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = parseResult.extract(dataService)

        assertEquals(
            LocalizedStrings(nb = null, nn = null, en = "description"),
            dataServiceExtraction.dataService.description!!
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(3, result.operations.size)

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
        val specification = """
            {
                "openapi": "3.0.0",
                "info": {
                    "version": "1.0",
                    "title": "title",
                    "contact": {
                        "name": "name",
                        "email": "invalid"
                    }
                },
                "servers": [
                    {
                        "url": "https://example.com"
                    }
                ],
                "paths": {}
            }
        """.trimIndent()

        val parseResult = OpenAPIParser().readContents(specification, null, null)


        val dataService = DataService(
            id = "id",
            catalogId = "catalogId",
            endpointUrl = "endpointUrl",
            title = LocalizedStrings()
        )

        val dataServiceExtraction = parseResult.extract(dataService)

        assertEquals(
            ContactPoint(name = LocalizedStrings(nb = null, nn = null, en = "name")),
            dataServiceExtraction.dataService.contactPoint!!
        )

        dataServiceExtraction.extractionRecord.extractResult.let { result ->
            assertEquals(3, result.operations.size)

            assertTrue(result.operations.any {
                it.op == OpEnum.REPLACE && it.path == "/contactPoint" && it.value == mapOf(
                    "name" to mapOf(
                        "nb" to null,
                        "nn" to null,
                        "en" to "name"
                    ),
                    "phone" to null,
                    "email" to null,
                    "url" to null
                )
            })

            assertEquals(1, result.issues.size)

            assertTrue(result.issues.any {
                it.type == IssueType.WARNING && it.message.contains("contact.email")
            })
        }
    }
}