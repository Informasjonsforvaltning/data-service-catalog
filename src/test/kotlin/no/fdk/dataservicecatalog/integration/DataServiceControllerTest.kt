package no.fdk.dataservicecatalog.integration

import no.fdk.dataservicecatalog.configuration.JacksonConfig
import no.fdk.dataservicecatalog.controller.DataServiceController
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
@WebMvcTest(controllers = [DataServiceController::class])
class DataServiceControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `should respond with not implemented on valid register data service`() {
        mockMvc.post("/internal/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": [
                        {
                            "language": "nb",
                            "value": "title"
                        }
                    ]
                }
            """
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `should respond with bad request on missing endpointUrl in register data service`() {
        mockMvc.post("/internal/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": null,
                    "titles": [
                        {
                            "language": "nb",
                            "value": "title"
                        }
                    ]
                }
            """
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
    fun `should respond with bad request on missing titles in register data service`() {
        mockMvc.post("/internal/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": null
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to read request") }
            jsonPath("$.errors[0].field") { value("titles") }
            jsonPath("$.errors[0].message") { value("Cannot be null or empty") }
        }
    }

    @Test
    fun `should respond with bad request on missing language for titles in register data service`() {
        mockMvc.post("/internal/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": [
                        {
                            "value": "title"
                        }
                    ]
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to read request") }
            jsonPath("$.errors[0].field") { value("titles[0].language") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun `should respond with bad request on missing value for titles in register data service`() {
        mockMvc.post("/internal/catalogs/12345/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": [
                        {
                            "language": "nb"
                        }
                    ]
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to read request") }
            jsonPath("$.errors[0].field") { value("titles[0].value") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }
}
