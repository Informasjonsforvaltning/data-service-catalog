package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.config.JacksonConfig
import no.fdk.dataservicecatalog.config.SecurityConfig
import no.fdk.dataservicecatalog.controller.ImportController
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import no.fdk.dataservicecatalog.handler.ImportHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
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
    fun `import openAPI should respond with created and payload`() {
        val catalogId = "1234"
        val resultId = "5678"

        handler.stub {
            on { importOpenApi(catalogId, "{}") } doReturn ImportResult(
                id = resultId,
                created = LocalDateTime.now(),
                catalogId = catalogId,
                status = ImportResultStatus.COMPLETED
            )
        }

        mockMvc.post("/import/$catalogId")
        {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isCreated() }
            header { string("Location", "/import/$catalogId/results/$resultId") }
        }
    }

    @Test
    fun `import openAPI should respond with forbidden on invalid authority`() {
        val catalogId = "1234"

        mockMvc.post("/import/$catalogId")
        {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
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

        mockMvc.get("/import/$catalogId/results")
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

        mockMvc.get("/import/$catalogId/results/$resultId")
        {
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isNotFound() }
        }
    }
}
