package no.fdk.dataservicecatalog.controller

import jakarta.validation.Valid
import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.PatchRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/data-services")
class DataServiceController {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServicesByCatalogId(@PathVariable catalogId: String): ResponseEntity<List<DataService>> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @GetMapping("/{dataServiceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<DataService> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun registerDataServiceByCatalogId(
        @PathVariable catalogId: String, @Valid @RequestBody dataService: DataService
    ): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @PatchMapping(
        "/{dataServiceId}", consumes = ["application/json-patch+json"], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
        @Valid @RequestBody patchRequest: PatchRequest
    ): ResponseEntity<DataService> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @DeleteMapping("/{dataServiceId}")
    fun deleteDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val problemDetail = ex.body

        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to fieldError.defaultMessage
            )
        }

        problemDetail.setProperty("errors", errors)

        return ResponseEntity.badRequest().body(problemDetail)
    }
}
