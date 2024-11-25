package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.config.JacksonConfig
import no.fdk.dataservicecatalog.config.SecurityConfig
import no.fdk.dataservicecatalog.controller.DataServiceController
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.CatalogNotFoundException
import no.fdk.dataservicecatalog.exception.DataServiceNotFoundException
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@Tag("integration")
@ActiveProfiles("test")

@Import(SecurityConfig::class, JacksonConfig::class)
@WebMvcTest(controllers = [DataServiceController::class])
class DataServiceControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockBean
    lateinit var handler: DataServiceHandler

    @ParameterizedTest
    @ValueSource(strings = ["system:root:admin", "organization:1234:admin", "organization:1234:write", "organization:1234:read"])
    fun `find should respond with ok and payload`(authority: String) {
        handler.stub {
            on { findAll("1234") } doReturn emptyList()
        }

        mockMvc.get("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority(authority)))
        }.andExpect {
            status { isOk() }
            content { string("[]") }
        }
    }

    @Test
    fun `find should respond with forbidden on invalid authority`() {
        mockMvc.get("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `find should respond with not found on exception`() {
        handler.stub {
            on { findAll("1234") } doThrow CatalogNotFoundException("Catalog 1234 not found")
        }

        mockMvc.get("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("system:root:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Catalog 1234 not found") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["system:root:admin", "organization:1234:admin", "organization:1234:write", "organization:1234:read"])
    fun `find by id should respond with ok and payload`(authority: String) {
        handler.stub {
            on { findById("1234", "5678") } doReturn DataService(id = "5678")
        }

        mockMvc.get("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority(authority)))
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value("5678") }
        }
    }

    @Test
    fun `find by id should respond with forbidden on invalid autority`() {
        mockMvc.get("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `find by id should respond with not found on exception`() {
        handler.stub {
            on { findById("1234", "5678") } doThrow DataServiceNotFoundException("Data Service 5678 not found")
        }

        mockMvc.get("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("system:root:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service 5678 not found") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:1234:admin", "organization:1234:write"])
    fun `register should respond with created and location on valid payload`(authority: String) {
        val dataService = DataService(
            endpointUrl = "endpointUrl",
            titles = listOf(LanguageString("nb", "title"))
        )

        handler.stub {
            on { register("1234", dataService) } doReturn "5678"
        }

        mockMvc.post("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority(authority)))
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
            status { isCreated() }
            header {
                string("location", "/internal/catalogs/1234/data-services/5678")
            }
        }
    }

    @Test
    fun `register should respond with forbidden on invalid authority`() {
        mockMvc.post("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
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
            status { isForbidden() }
        }
    }

    @Test
    fun `register should respond with not found on exception`() {
        val dataService = DataService(
            endpointUrl = "endpointUrl",
            titles = listOf(LanguageString("nb", "title"))
        )

        handler.stub {
            on { register("1234", dataService) } doThrow CatalogNotFoundException("Catalog 1234 not found")
        }

        mockMvc.post("/internal/catalogs/1234/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:1234:admin")))
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
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Catalog 1234 not found") }
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

    @ParameterizedTest
    @ValueSource(strings = ["organization:1234:admin", "organization:1234:write"])
    fun `update should respond with ok and payload`(authority: String) {
        val patchRequest = PatchRequest(
            listOf(
                JsonPatchOperation(
                    op = OpEnum.REMOVE,
                    path = "titles"
                )
            )
        )

        handler.stub {
            on { update("1234", "5678", patchRequest) } doReturn DataService(endpointUrl = "endpointUrl")
        }

        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority(authority)))
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": "remove",
                        "path": "titles"
                    }
                ]
            """
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `update should respond with forbidden on invalid authority`() {
        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
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
            status { isForbidden() }
        }
    }

    @Test
    fun `update should respond with not found on exception`() {
        val patchRequest = PatchRequest(
            listOf(
                JsonPatchOperation(
                    op = OpEnum.REMOVE,
                    path = "titles"
                )
            )
        )

        handler.stub {
            on {
                update(
                    "1234",
                    "5678",
                    patchRequest
                )
            } doThrow DataServiceNotFoundException("Data Service 5678 not found")
        }

        mockMvc.patch("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:1234:admin")))
            contentType = MediaType.valueOf("application/json-patch+json")
            content = """
                [
                    {
                        "op": "remove",
                        "path": "titles"
                    }
                ]
            """
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service 5678 not found") }
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

    @ParameterizedTest
    @ValueSource(strings = ["organization:1234:admin", "organization:1234:write"])
    fun `delete should respond with no content`(authority: String) {
        mockMvc.delete("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority(authority)))
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `delete should respond with forbidden on invalid authority`() {
        mockMvc.delete("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `delete should respond with not found on exception`() {
        handler.stub {
            on { delete("1234", "5678") } doThrow DataServiceNotFoundException("Data Service 5678 not found")
        }

        mockMvc.delete("/internal/catalogs/1234/data-services/5678") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:1234:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service 5678 not found") }
        }
    }
}
