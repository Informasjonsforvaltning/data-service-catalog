package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.config.JacksonConfig
import no.fdk.dataservicecatalog.config.SecurityConfig
import no.fdk.dataservicecatalog.controller.DataServiceController
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.NotFoundException
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
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.*

@Tag("integration")
@ActiveProfiles("test")

@Import(SecurityConfig::class, JacksonConfig::class)
@WebMvcTest(controllers = [DataServiceController::class])
class DataServiceControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var handler: DataServiceHandler

    @ParameterizedTest
    @ValueSource(strings = ["system:root:admin", "organization:%s:admin", "organization:%s:write", "organization:%s:read"])
    fun `find should respond with ok and payload`(authority: String) {
        val catalogId = "1234"

        handler.stub {
            on { findAll(catalogId) } doReturn emptyList()
        }

        mockMvc.get("/internal/catalogs/$catalogId/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
        }.andExpect {
            status { isOk() }
            content { json("[]") }
        }
    }

    @Test
    fun `find should respond with forbidden on invalid authority`() {
        val catalogId = "1234"

        mockMvc.get("/internal/catalogs/$catalogId/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["system:root:admin", "organization:%s:admin", "organization:%s:write", "organization:%s:read"])
    fun `find by id should respond with ok and payload`(authority: String) {
        val catalogId = "1234"
        val dataServiceId = "5678"

        handler.stub {
            on { findById(catalogId, dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = catalogId,
                status = Status.PUBLISHED,
                endpointUrl = "endpointUrl",
                titles = listOf(
                    LanguageString(language = "nb", value = "title")
                )
            )
        }

        mockMvc.get("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
        }.andExpect {
            status { isOk() }
            jsonPath("$.endpointUrl") { value("endpointUrl") }
        }
    }

    @Test
    fun `find by id should respond with forbidden on invalid authority`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        mockMvc.get("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `find by id should respond with not found on exception`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        handler.stub {
            on { findById(catalogId, dataServiceId) } doThrow NotFoundException("Data Service $dataServiceId not found")
        }

        mockMvc.get("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("system:root:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service $dataServiceId not found") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:%s:admin", "organization:%s:write"])
    fun `register should respond with created and location on valid payload`(authority: String) {
        val catalogId = "1234"
        val dataServiceId = "5678"

        val registerDataService = RegisterDataService(
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        handler.stub {
            on { register(catalogId, registerDataService) } doReturn dataServiceId
        }

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
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
                string("location", "/internal/catalogs/$catalogId/data-services/$dataServiceId")
            }
        }
    }

    @Test
    fun `register should respond with forbidden on invalid authority`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
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
        val catalogId = "1234"

        val registerDataService = RegisterDataService(
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        handler.stub {
            on { register(catalogId, registerDataService) } doThrow NotFoundException("Catalog $catalogId not found")
        }

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:$catalogId:admin")))
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
    fun `register should respond with bad request on blank endpointUrl in payload`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "",
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
            jsonPath("$.detail") { value("Failed to validate content.") }
            jsonPath("$.errors[0].field") { value("endpointUrl") }
            jsonPath("$.errors[0].message") { value("Cannot be blank") }
        }
    }

    @Test
    fun `register should respond with bad request on empty titles in payload`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": []
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to validate content.") }
            jsonPath("$.errors[0].field") { value("titles") }
            jsonPath("$.errors[0].message") { value("Cannot be empty") }
        }
    }

    @Test
    fun `register should respond with bad request on blank language for titles in payload`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": [
                        {
                            "language": "",
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
            jsonPath("$.detail") { value("Failed to validate content.") }
            jsonPath("$.errors[0].field") { value("titles[0].language") }
            jsonPath("$.errors[0].message") { value("Cannot be blank") }
        }
    }

    @Test
    fun `register should respond with bad request on blank value for titles in payload`() {
        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/data-services") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "endpointUrl": "endpointUrl",
                    "titles": [
                        {
                            "language": "nb",
                            "value": ""
                        }
                    ]
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to validate content.") }
            jsonPath("$.errors[0].field") { value("titles[0].value") }
            jsonPath("$.errors[0].message") { value("Cannot be blank") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:%s:admin", "organization:%s:write"])
    fun `update should respond with ok and payload`(authority: String) {
        val catalogId = "1234"
        val dataServiceId = "5678"

        val dataService = DataService(
            id = dataServiceId,
            catalogId = catalogId,
            status = Status.PUBLISHED,
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        val patchRequest = PatchRequest(
            listOf(
                JsonPatchOperation(
                    op = OpEnum.REMOVE,
                    path = "titles"
                )
            )
        )

        handler.stub {
            on { update(catalogId, dataServiceId, patchRequest) } doReturn dataService
        }

        mockMvc.patch("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
            contentType = MediaType.APPLICATION_JSON
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
        val catalogId = "1234"
        val dataServiceId = "5678"

        mockMvc.patch("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
            contentType = MediaType.APPLICATION_JSON
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
        val catalogId = "1234"
        val dataServiceId = "5678"

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
                    catalogId,
                    dataServiceId,
                    patchRequest
                )
            } doThrow NotFoundException("Data Service $dataServiceId not found")
        }

        mockMvc.patch("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:$catalogId:admin")))
            contentType = MediaType.APPLICATION_JSON
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
            jsonPath("$.detail") { value("Data Service $dataServiceId not found") }
        }
    }

    @Test
    fun `update should respond with bad request on blank path in payload`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        mockMvc.patch("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt())
            contentType = MediaType.APPLICATION_JSON
            content = """
                [
                    {
                        "op": "add",
                        "path": ""
                    }
                ]
                
            """
        }.andExpect {
            status { isBadRequest() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Failed to validate content.") }
            jsonPath("$.errors[0].field") { value("patchOperations[0].path") }
            jsonPath("$.errors[0].message") { value("Cannot be blank") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:%s:admin", "organization:%s:write"])
    fun `delete should respond with no content`(authority: String) {
        val catalogId = "1234"
        val dataServiceId = "5678"

        mockMvc.delete("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `delete should respond with forbidden on invalid authority`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        mockMvc.delete("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("invalid")))
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `delete should respond with not found on exception`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        handler.stub {
            on { delete(catalogId, dataServiceId) } doThrow NotFoundException("Data Service $dataServiceId not found")
        }

        mockMvc.delete("/internal/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt().authorities(SimpleGrantedAuthority("organization:$catalogId:admin")))
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service $dataServiceId not found") }
        }
    }
}
