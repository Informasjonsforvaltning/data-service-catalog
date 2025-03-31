package no.fdk.catalog.dataservice.importer.controller

import no.fdk.catalog.dataservice.core.exception.NotFoundException
import no.fdk.catalog.dataservice.importer.domain.ImportResult
import no.fdk.catalog.dataservice.importer.exception.OpenApiParseException
import no.fdk.catalog.dataservice.importer.handler.ImportHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/import")
class ImportController(private val importHandler: ImportHandler) {

    @PreAuthorize(ADMIN)
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_YAML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun import(@PathVariable catalogId: String, @RequestBody dataService: String): ResponseEntity<Void> {
        val importResult = importHandler.importOpenApi(catalogId, dataService)

        return ResponseEntity
            .created(URI("/internal/catalogs/${catalogId}/import/results/${importResult.id}"))
            .build()
    }

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(@PathVariable catalogId: String): ResponseEntity<List<ImportResult>> {
        return ResponseEntity.ok(importHandler.getResults(catalogId))
    }

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(
        @PathVariable catalogId: String,
        @PathVariable id: String
    ): ResponseEntity<ImportResult> {
        return importHandler.getResult(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping(value = ["/results/{id}"])
    fun delete(@PathVariable catalogId: String, @PathVariable id: String): ResponseEntity<Void> {
        return importHandler.deleteResult(catalogId, id).let { ResponseEntity.noContent().build() }
    }

    @ExceptionHandler
    fun handleOpenApiParseException(ex: OpenApiParseException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message)

        return ResponseEntity.of(problemDetail).build()
    }

    @ExceptionHandler
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)).build()
    }

    companion object {
        const val ADMIN =
            "hasAuthority('organization:' + #catalogId + ':admin')"
    }
}
