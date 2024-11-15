package no.fdk.dataservicecatalog.integration

import no.fdk.dataservicecatalog.configuration.JacksonConfig
import no.fdk.dataservicecatalog.controller.CatalogController
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@Tag("integration")
@ActiveProfiles("test")
@Import(JacksonConfig::class)
@WebMvcTest(controllers = [CatalogController::class])
class CatalogControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun shouldRespondWithCreatedOnValidDataServiceObject() {
        mockMvc.post("/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "title": {
                        "nb": "title"
                    }
                }
            """.trimIndent()
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun shouldRespondWithBadRequestOnMissingEndpointUrlInDataServiceObject() {
        mockMvc.post("/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": null,
                    "title": {
                        "nb": "title"
                    }
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to read request") }
            jsonPath("$.errors[0].field") { value("endpointUrl") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun shouldRespondWithBadRequestOnMissingTitleInDataServiceObject() {
        mockMvc.post("/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "title": null
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to read request") }
            jsonPath("$.errors[0].field") { value("title") }
            jsonPath("$.errors[0].message") { value("Cannot be null or empty") }
        }
    }
}
