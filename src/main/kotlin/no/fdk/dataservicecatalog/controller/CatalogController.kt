package no.fdk.dataservicecatalog.controller

import no.fdk.dataservicecatalog.controller.CatalogController.Companion.JSON_LD
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N3
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N_QUADS
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.N_TRIPLES
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.RDF_JSON
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.RDF_XML
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TRIG
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TRIX
import no.fdk.dataservicecatalog.controller.CatalogController.Companion.TURTLE
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFLanguages
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalogs", produces = [N3, TURTLE, RDF_XML, RDF_JSON, JSON_LD, TRIX, TRIG, N_QUADS, N_TRIPLES])
class CatalogController {

    @GetMapping
    fun findCatalogs(
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = TURTLE) acceptHeader: String
    ): ResponseEntity<String> {
        val lang = getRDFLang(acceptHeader)

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @GetMapping("/{catalogId}")
    fun findCatalogById(
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = TURTLE) acceptHeader: String
    ): ResponseEntity<String> {
        val lang = getRDFLang(acceptHeader)

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    companion object {
        const val N3 = "text/n3"
        const val TURTLE = "text/turtle"
        const val RDF_XML = "application/rdf+xml"
        const val RDF_JSON = "application/rdf+json"
        const val JSON_LD = "application/ld+json"
        const val TRIX = "application/trix"
        const val TRIG = "application/trig"
        const val N_QUADS = "application/n-quads"
        const val N_TRIPLES = "application/n-triples"

        fun getRDFLang(accept: String): Lang {
            return RDFLanguages.contentTypeToLang(accept) ?: Lang.TURTLE
        }
    }
}
