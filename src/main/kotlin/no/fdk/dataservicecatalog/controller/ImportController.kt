package no.fdk.dataservicecatalog.controller

import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.exception.OpenApiParseException
import no.fdk.dataservicecatalog.handler.ImportHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping(value = ["/import/{catalogId}"])
class ImportController(private val importHandler: ImportHandler) {

    @PreAuthorize(ADMIN)
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_YAML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun import(@PathVariable catalogId: String, @RequestBody dataService: String): ResponseEntity<Void> {
        val importResult = importHandler.importOpenApi(catalogId, dataService)

        return ResponseEntity
            .created(URI("/import/$catalogId/results/${importResult.id}"))
            .build()
    }

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(@PathVariable catalogId: String): ResponseEntity<List<ImportResult>> {
        return ResponseEntity.ok(importHandler.getResults(catalogId))
    }

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(@PathVariable catalogId: String, @PathVariable id: String): ResponseEntity<ImportResult> {
        return importHandler.getResult(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleOpenApiParseException(ex: OpenApiParseException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to parse OpenAPI.")

        if (ex.messages.isNotEmpty()) {
            val errors = ex.messages.map { message -> mapOf("message" to message) }
            problemDetail.setProperty("errors", errors)
        }

        return ResponseEntity.of(problemDetail).build()
    }

    companion object {
        const val ADMIN =
            "hasAuthority('organization:' + #catalogId + ':admin')"
    }
}
