package no.fdk.dataservicecatalog.controller

import jakarta.validation.Valid
import no.fdk.dataservicecatalog.domain.FindDataServiceQuery
import no.fdk.dataservicecatalog.domain.JsonPatchOperation
import no.fdk.dataservicecatalog.domain.RegisterDataServiceCommand
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/catalogs")
class DataServiceController {

    @GetMapping("{catalogId}/data-services", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServicesByCatalogId(@PathVariable catalogId: String): ResponseEntity<List<FindDataServiceQuery>> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @GetMapping("{catalogId}/data-services/{dataServiceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<FindDataServiceQuery> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @PostMapping("{catalogId}/data-services", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun registerDataServiceByCatalogId(
        @PathVariable catalogId: String, @Valid @RequestBody registerDataServiceCommand: RegisterDataServiceCommand
    ): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @PatchMapping(
        "{catalogId}/data-services/{dataServiceId}",
        consumes = ["application/json-patch+json"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @DeleteMapping("{catalogId}/data-services/{dataServiceId}")
    fun deleteDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to read request")

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