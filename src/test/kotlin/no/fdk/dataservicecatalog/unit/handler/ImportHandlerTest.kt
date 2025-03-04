package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.handler.ImportHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import no.fdk.dataservicecatalog.repository.ImportResultRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@Tag("unit")
@ExtendWith(MockitoExtension::class)
class ImportHandlerTest {

    @Mock
    lateinit var dataServiceRepository: DataServiceRepository

    @Mock
    lateinit var importResultRepository: ImportResultRepository

    @InjectMocks
    lateinit var handler: ImportHandler

    @Test
    fun `import open api`() {
        val catalogId = "1234"

        val specification = """
            {
                "openapi": "3.0.0",
                "info": {
                    "title": "Open API",
                    "version": "1.0"
                },
                "paths": {}
            }
        """.trimIndent()

        val importResult = handler.importOpenApi(catalogId, specification)

        assertEquals(ImportResultStatus.COMPLETED, importResult.status)
    }
}