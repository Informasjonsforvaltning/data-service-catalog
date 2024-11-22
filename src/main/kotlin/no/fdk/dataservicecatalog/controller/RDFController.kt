package no.fdk.dataservicecatalog.controller

import no.fdk.dataservicecatalog.controller.RDFController.Companion.JSON_LD
import no.fdk.dataservicecatalog.controller.RDFController.Companion.N3
import no.fdk.dataservicecatalog.controller.RDFController.Companion.N_QUADS
import no.fdk.dataservicecatalog.controller.RDFController.Companion.N_TRIPLES
import no.fdk.dataservicecatalog.controller.RDFController.Companion.RDF_JSON
import no.fdk.dataservicecatalog.controller.RDFController.Companion.RDF_XML
import no.fdk.dataservicecatalog.controller.RDFController.Companion.TRIG
import no.fdk.dataservicecatalog.controller.RDFController.Companion.TRIX
import no.fdk.dataservicecatalog.controller.RDFController.Companion.TURTLE
import no.fdk.dataservicecatalog.exception.CatalogNotFoundException
import no.fdk.dataservicecatalog.exception.DataServiceNotFoundException
import no.fdk.dataservicecatalog.handler.RDFHandler
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFLanguages
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/catalogs", produces = [N3, TURTLE, RDF_XML, RDF_JSON, JSON_LD, TRIX, TRIG, N_QUADS, N_TRIPLES])
class RDFController(private val handler: RDFHandler) {

    @GetMapping
    fun findCatalogs(
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = TURTLE) acceptHeader: String
    ): ResponseEntity<String> {
        return getRDFLang(acceptHeader)
            .let { handler.findAll(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{catalogId}")
    fun findCatalogById(
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = TURTLE) acceptHeader: String,
        @PathVariable catalogId: String
    ): ResponseEntity<String> {
        return getRDFLang(acceptHeader)
            .let { handler.findById(catalogId, it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{catalogId}/data-services/{dataServiceId}")
    fun findDataServiceByCatalogIdAndDataServiceId(
        @RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = TURTLE) acceptHeader: String,
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<String> {
        return getRDFLang(acceptHeader)
            .let { handler.findById(catalogId, dataServiceId, it) }
            .let { ResponseEntity.ok(it) }
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

    @ExceptionHandler(CatalogNotFoundException::class, DataServiceNotFoundException::class)
    fun handleNotFoundException(ex: RuntimeException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)).build()
    }
}
