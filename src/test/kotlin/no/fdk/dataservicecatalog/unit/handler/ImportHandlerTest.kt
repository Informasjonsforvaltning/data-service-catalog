package no.fdk.dataservicecatalog.unit.handler

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.core.models.SwaggerParseResult
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.handler.ImportHandler
import no.fdk.dataservicecatalog.service.ImportOpenApiService
import no.fdk.dataservicecatalog.service.ImportResultService
import no.fdk.dataservicecatalog.service.ImportService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@Tag("unit")
@ExtendWith(MockitoExtension::class)
class ImportHandlerTest {

    @Mock
    lateinit var importService: ImportService

    @Mock
    lateinit var importResultService: ImportResultService

    @Mock
    lateinit var importOpenApiService: ImportOpenApiService

    @InjectMocks
    lateinit var handler: ImportHandler

    @Test
    fun `should throw exception on invalid open api`() {
        val catalogId = "1234"

        val parseResult = SwaggerParseResult()

        importOpenApiService.stub {
            on { parse(any()) } doReturn parseResult
        }

        assertThrows<OpenApiParseException> {
            handler.importOpenApi(catalogId, "specification")
        }
    }

    @Test
    fun `should throw exception if too many servers in open api`() {
        val catalogId = "1234"

        val server = Server()
        server.url = "https://example.com"

        val openAPI = OpenAPI()
        openAPI.servers = listOf(server, server)

        val parseResult = SwaggerParseResult()
        parseResult.openAPI = openAPI

        importOpenApiService.stub {
            on { parse(any()) } doReturn parseResult
        }

        assertThrows<OpenApiParseException> {
            handler.importOpenApi(catalogId, "specification")
        }
    }

    @Test
    fun `should throw exception on invalid url for servers in open api`() {
        val catalogId = "1234"

        val server = Server()
        server.url = "example.com"

        val openAPI = OpenAPI()
        openAPI.servers = listOf(server)

        val parseResult = SwaggerParseResult()
        parseResult.openAPI = openAPI

        importOpenApiService.stub {
            on { parse(any()) } doReturn parseResult
        }

        assertThrows<OpenApiParseException> {
            handler.importOpenApi(catalogId, "specification")
        }
    }

    @Test
    fun `should import open api from existing data service`() {
        val specification = "specification"

        val catalogId = "catalogId"
        val internalId = "internalId"
        val externalId = "https://example.com"

        val server = Server()
        server.url = externalId

        val openAPI = OpenAPI()
        openAPI.servers = listOf(server)

        val parseResult = SwaggerParseResult()
        parseResult.openAPI = openAPI

        importOpenApiService.stub {
            on { parse(specification) } doReturn parseResult
        }

        val dataService = DataService(
            id = internalId,
            catalogId = catalogId,
            endpointUrl = externalId,
            title = LocalizedStrings()
        )

        importResultService.stub {
            on { findDataServiceIdByCatalogIdAndExternalId(catalogId, externalId) } doReturn internalId
        }

        importService.stub {
            on { findDataService(internalId) } doReturn dataService
        }

        val importResult = ImportResult(
            id = "",
            catalogId = catalogId,
            status = ImportResultStatus.COMPLETED,
            extractionRecords = listOf(
                ExtractionRecord(
                    internalId = internalId,
                    externalId = externalId,
                    extractResult = ExtractResult()
                )
            )
        )

        importOpenApiService.stub {
            on { extract(parseResult, dataService) } doReturn importResult
        }

        importService.stub {
            on { save(dataService) } doReturn dataService
        }

        importResultService.stub {
            on { save(importResult) } doReturn importResult
        }

        val result = handler.importOpenApi(catalogId, specification)

        assertEquals(ImportResultStatus.COMPLETED, result.status)
    }

    @Test
    fun `should import open api from new data service`() {
        val specification = "specification"

        val catalogId = "catalogId"
        val internalId = "internalId"
        val externalId = "https://example.com"

        val server = Server()
        server.url = externalId

        val openAPI = OpenAPI()
        openAPI.servers = listOf(server)

        val parseResult = SwaggerParseResult()
        parseResult.openAPI = openAPI

        importOpenApiService.stub {
            on { parse(specification) } doReturn parseResult
        }

        val dataService = DataService(
            id = internalId,
            catalogId = catalogId,
            endpointUrl = externalId,
            title = LocalizedStrings()
        )

        importResultService.stub {
            on { findDataServiceIdByCatalogIdAndExternalId(catalogId, externalId) } doReturn null
        }

        importService.stub {
            on { createDataService(externalId, catalogId) } doReturn dataService
        }

        val importResult = ImportResult(
            id = "",
            catalogId = catalogId,
            status = ImportResultStatus.COMPLETED,
            extractionRecords = listOf(
                ExtractionRecord(
                    internalId = internalId,
                    externalId = externalId,
                    extractResult = ExtractResult()
                )
            )
        )

        importOpenApiService.stub {
            on { extract(parseResult, dataService) } doReturn importResult
        }

        importService.stub {
            on { save(dataService) } doReturn dataService
        }

        importResultService.stub {
            on { save(importResult) } doReturn importResult
        }

        val result = handler.importOpenApi(catalogId, specification)

        assertEquals(ImportResultStatus.COMPLETED, result.status)
    }

    @Test
    fun `should fail to import open api from new data service`() {
        val specification = "specification"

        val catalogId = "catalogId"
        val internalId = "internalId"
        val externalId = "https://example.com"

        val server = Server()
        server.url = externalId

        val openAPI = OpenAPI()
        openAPI.servers = listOf(server)

        val parseResult = SwaggerParseResult()
        parseResult.openAPI = openAPI

        importOpenApiService.stub {
            on { parse(specification) } doReturn parseResult
        }

        val dataService = DataService(
            id = internalId,
            catalogId = catalogId,
            endpointUrl = externalId,
            title = LocalizedStrings()
        )

        importResultService.stub {
            on { findDataServiceIdByCatalogIdAndExternalId(catalogId, externalId) } doReturn null
        }

        importService.stub {
            on { createDataService(externalId, catalogId) } doReturn dataService
        }

        val importResult = ImportResult(
            id = "",
            catalogId = catalogId,
            status = ImportResultStatus.FAILED,
            extractionRecords = listOf(
                ExtractionRecord(
                    internalId = internalId,
                    externalId = externalId,
                    extractResult = ExtractResult()
                )
            )
        )

        importOpenApiService.stub {
            on { extract(parseResult, dataService) } doReturn importResult
        }

        importResultService.stub {
            on { save(importResult) } doReturn importResult
        }

        val result = handler.importOpenApi(catalogId, specification)

        assertEquals(ImportResultStatus.FAILED, result.status)
    }
}
