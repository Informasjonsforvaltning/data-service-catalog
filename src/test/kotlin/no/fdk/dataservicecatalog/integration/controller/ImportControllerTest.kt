package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.config.JacksonConfig
import no.fdk.dataservicecatalog.config.SecurityConfig
import no.fdk.dataservicecatalog.controller.ImportController
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.handler.ImportHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@Tag("integration")
@ActiveProfiles("test")

@Import(SecurityConfig::class, JacksonConfig::class)
@WebMvcTest(controllers = [ImportController::class])
class ImportControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var handler: ImportHandler

    @Test
    fun `import openAPI should respond with created`() {
        val specification = "specification"

        val catalogId = "1234"
        val resultId = "5678"

        handler.stub {
            on { importOpenApi(catalogId, specification) } doReturn ImportResult(
                id = resultId,
                created = LocalDateTime.now(),
                catalogId = catalogId,
                status = ImportResultStatus.COMPLETED
            )
        }

        mockMvc.post("/internal/catalogs/$catalogId/import")
        {
            contentType = MediaType.APPLICATION_JSON
            content = specification
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isCreated() }
            header { string("Location", "/internal/catalogs/$catalogId/import/results/$resultId") }
        }
    }

    @Test
    fun `import openAPI should respond with bad request on exception`() {
        val specification = "specification"

        val catalogId = "1234"

        handler.stub {
            on { importOpenApi(catalogId, specification) } doThrow OpenApiParseException("Failed to parse OpenAPI.")
        }

        mockMvc.post("/internal/catalogs/$catalogId/import")
        {
            contentType = MediaType.APPLICATION_JSON
            content = specification
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to parse OpenAPI.") }
        }
    }

    @Test
    fun `import openAPI should respond with forbidden on invalid authority`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/import")
        {
            contentType = MediaType.APPLICATION_JSON
            content = "specification"
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `get results should respond with ok and payload`() {
        val catalogId = "1234"

        handler.stub {
            on { getResults(catalogId) } doReturn emptyList()
        }

        mockMvc.get("/internal/catalogs/$catalogId/import/results")
        {
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `get result should respond with not found`() {
        val catalogId = "1234"
        val resultId = "5678"

        handler.stub {
            on { getResult(catalogId) } doReturn null
        }

        mockMvc.get("/internal/catalogs/$catalogId/import/results/$resultId")
        {
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `delete should respond with no content`() {
        val catalogId = "1234"
        val resultId = "5678"

        mockMvc.delete("/internal/catalogs/$catalogId/import/results/$resultId") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:$catalogId:admin")))
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `delete should respond with forbidden on invalid authority`() {
        val catalogId = "1234"
        val resultId = "5678"

        mockMvc.delete("/internal/catalogs/$catalogId/import/results/$resultId") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `delete should respond with not found on exception`() {
        val catalogId = "1234"
        val resultId = "5678"

        handler.stub {
            on { deleteResult(catalogId, resultId) } doThrow NotFoundException("Import result $resultId not found")
        }

        mockMvc.delete("/internal/catalogs/$catalogId/import/results/$resultId") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:$catalogId:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Import result $resultId not found") }
        }
    }
}
