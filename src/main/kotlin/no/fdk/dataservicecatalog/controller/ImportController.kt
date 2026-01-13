package no.fdk.dataservicecatalog.controller

import no.fdk.dataservicecatalog.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/import")
class ImportController() {

    @PreAuthorize(ADMIN)
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_YAML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun import(@PathVariable catalogId: String, @RequestBody dataService: String): ResponseEntity<Void> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(@PathVariable catalogId: String): ResponseEntity<List<Void>> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    @PreAuthorize(ADMIN)
    @GetMapping(value = ["/results/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun result(@PathVariable catalogId: String, @PathVariable id: String): ResponseEntity<Void> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    @PreAuthorize(ADMIN)
    @DeleteMapping(value = ["/results/{id}"])
    fun delete(@PathVariable catalogId: String, @PathVariable id: String): ResponseEntity<Void> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)

    @ExceptionHandler
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)).build()
    }

    companion object {
        const val ADMIN =
            "hasAuthority('organization:' + #catalogId + ':admin')"
    }
}
