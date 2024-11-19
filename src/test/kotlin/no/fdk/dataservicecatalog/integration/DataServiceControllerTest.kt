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
import org.springframework.test.web.servlet.*

@Tag("integration")
@ActiveProfiles("test")
@Import(JacksonConfig::class)
@WebMvcTest(controllers = [DataServiceController::class])
class DataServiceControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `count should respond with not implemented`() {
        mockMvc.get("/internal/catalogs/count") {
            with(jwt())
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `find should respond with not implemented`() {
        mockMvc.get("/internal/catalogs/1234/data-services") {
            with(jwt())
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `find by id should respond with not implemented`() {
        mockMvc.get("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `register should respond with not implemented on valid payload`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
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
    fun `register should respond with bad request on missing endpointUrl in payload`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
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
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("endpointUrl") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun `register should respond with bad request on missing titles in payload`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
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
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("titles") }
            jsonPath("$.errors[0].message") { value("Cannot be null or empty") }
        }
    }

    @Test
    fun `register should respond with bad request on missing language for titles in payload`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
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
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("titles[0].language") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun `register should respond with bad request on missing value for titles in payload`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
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
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("titles[0].value") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun `update should respond with not implemented`() {
        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": "add",
                        "path": "path"
                    }
                ]
            """
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `update should respond with bad request on invalid op in payload`() {
        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": "invalid",
                        "path": "path"
                    }
                ]
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("patchOperations[0].op") }
            jsonPath("$.errors[0].message") { value("Cannot be null or invalid operator") }
        }
    }

    @Test
    fun `update should respond with bad request on missing op in payload`() {
        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": null,
                        "path": "path"
                    }
                ]
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("patchOperations[0].op") }
            jsonPath("$.errors[0].message") { value("Cannot be null or invalid operator") }
        }
    }

    @Test
    fun `update should respond with bad request on missing path in payload`() {
        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": "add",
                        "path": null
                    }
                ]
                
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Invalid request content.") }
            jsonPath("$.errors[0].field") { value("patchOperations[0].path") }
            jsonPath("$.errors[0].message") { value("Cannot be null or blank") }
        }
    }

    @Test
    fun `delete should respond with not implemented`() {
        mockMvc.delete("/internal/catalogs/1234/data-services/5678") {
            with(jwt())
        }.andExpect {
            status { isNotImplemented() }
        }
    }
}
