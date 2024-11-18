package no.fdk.dataservicecatalog.integration

import no.fdk.dataservicecatalog.configuration.JacksonConfig
import no.fdk.dataservicecatalog.controller.CatalogController
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.JSON_LD
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N3
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N_QUADS
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N_TRIPLES
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.RDF_JSON
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.RDF_XML
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TRIG
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TRIX
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TURTLE
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Tag("integration")
@ActiveProfiles("test")
@Import(JacksonConfig::class)
@WebMvcTest(controllers = [CatalogController::class])
class CatalogControllerTest(@Autowired val mockMvc: MockMvc) {

    @ParameterizedTest
    @ValueSource(strings = [N3, TURTLE, RDF_XML, RDF_JSON, JSON_LD, TRIX, TRIG, N_QUADS, N_TRIPLES])
    fun `should respond with not implemented on valid media type`(mediaType: String) {
        mockMvc.get("/catalogs") {
            with(jwt())
            accept = MediaType.valueOf(mediaType)
        }.andExpect {
            status { isNotImplemented() }
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
}
