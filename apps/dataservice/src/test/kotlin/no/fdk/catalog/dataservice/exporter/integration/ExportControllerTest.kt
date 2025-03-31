package no.fdk.catalog.dataservice.exporter.integration

import no.fdk.catalog.dataservice.config.JacksonConfig
import no.fdk.catalog.dataservice.config.SecurityConfig
import no.fdk.catalog.dataservice.core.exception.NotFoundException
import no.fdk.catalog.dataservice.exporter.controller.ExportController
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.JSON_LD
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.N3
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.N_QUADS
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.N_TRIPLES
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.RDF_JSON
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.RDF_XML
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.TRIG
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.TRIX
import no.fdk.catalog.dataservice.exporter.controller.ExportController.Companion.TURTLE
import no.fdk.catalog.dataservice.exporter.handler.RDFHandler
import org.apache.jena.riot.Lang
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Tag("integration")
@ActiveProfiles("test")

@Import(SecurityConfig::class, JacksonConfig::class)
@WebMvcTest(controllers = [ExportController::class])
class ExportControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var handler: RDFHandler

    @ParameterizedTest
    @ValueSource(strings = [N3, TURTLE, RDF_XML, RDF_JSON, JSON_LD, TRIX, TRIG, N_QUADS, N_TRIPLES])
    fun `should respond with ok on valid media type`(mediaType: String) {
        mockMvc.get("/catalogs") {
            with(jwt())
            accept = MediaType.valueOf(mediaType)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should respond with not acceptable on invalid media type`() {
        mockMvc.get("/catalogs") {
            with(jwt())
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotAcceptable() }
        }
    }

    @Test
    fun `find should respond with ok and payload`() {
        handler.stub {
            on { findCatalogs(Lang.N3) } doReturn "turtle"
        }

        mockMvc.get("/catalogs") {
            with(jwt())
            accept = MediaType.valueOf(N3)
        }.andExpect {
            status { isOk() }
            content { string("turtle") }
        }
    }

    @Test
    fun `find by id should respond with ok and payload`() {
        val catalogId = "1234"

        handler.stub {
            on { findCatalogById(catalogId, Lang.N3) } doReturn "turtle"
        }

        mockMvc.get("/catalogs/$catalogId") {
            with(jwt())
            accept = MediaType.valueOf(N3)
        }.andExpect {
            status { isOk() }
            content { string("turtle") }
        }
    }


    @Test
    fun `find by id should respond with not found on exception`() {
        val catalogId = "1234"

        handler.stub {
            on { findCatalogById(catalogId, Lang.N3) } doThrow NotFoundException("Catalog $catalogId not found")
        }

        mockMvc.get("/catalogs/$catalogId") {
            with(jwt())
            accept = MediaType.valueOf(N3)
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Catalog $catalogId not found") }
        }
    }

    @Test
    fun `find data service by id should respond with ok and payload`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        handler.stub {
            on { findDataServiceByCatalogIdAndDataServiceId(catalogId, dataServiceId, Lang.N3) } doReturn "turtle"
        }

        mockMvc.get("/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt())
            accept = MediaType.valueOf(N3)
        }.andExpect {
            status { isOk() }
            content { string("turtle") }
        }
    }

    @Test
    fun `find data service by id should respond with not found on exception`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        handler.stub {
            on {
                findDataServiceByCatalogIdAndDataServiceId(catalogId, dataServiceId, Lang.N3)
            } doThrow NotFoundException("Data Service $dataServiceId not found")
        }

        mockMvc.get("/catalogs/$catalogId/data-services/$dataServiceId") {
            with(jwt())
            accept = MediaType.valueOf(N3)
        }.andExpect {
            status { isNotFound() }
            header {
                string("content-type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            }
            jsonPath("$.detail") { value("Data Service $dataServiceId not found") }
        }
    }
}
