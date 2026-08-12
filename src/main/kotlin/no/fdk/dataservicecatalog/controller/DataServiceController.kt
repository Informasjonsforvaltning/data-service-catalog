package no.fdk.dataservicecatalog.controller

import com.fasterxml.jackson.core.JsonProcessingException
import jakarta.validation.Valid
import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.DataServiceValues
import no.fdk.dataservicecatalog.domain.JsonPatchOperation
import no.fdk.dataservicecatalog.exception.BadRequestException
import no.fdk.dataservicecatalog.exception.InternalServerErrorException
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/data-services")
class DataServiceController(
    private val handler: DataServiceHandler,
) {
    @PreAuthorize(READ)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServicesByCatalogId(
        @PathVariable catalogId: String,
    ): ResponseEntity<List<DataService>> =
        handler
            .findAll(catalogId)
            .let { ResponseEntity.ok(it) }

    @PreAuthorize(READ)
    @GetMapping("/{dataServiceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
    ): ResponseEntity<DataService> =
        handler
            .findById(catalogId, dataServiceId)
            .let { ResponseEntity.ok(it) }

    @PreAuthorize(WRITE)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun registerDataServiceByCatalogId(
        @PathVariable catalogId: String,
        @Valid @RequestBody registerDataService: DataServiceValues,
    ): ResponseEntity<Void> =
        handler
            .register(catalogId, registerDataService)
            .let {
                ResponseEntity
                    .created(URI("/internal/catalogs/$catalogId/data-services/$it"))
                    .build()
            }

    @PreAuthorize(WRITE)
    @PatchMapping(
        "/{dataServiceId}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
        @Valid @RequestBody operations: List<JsonPatchOperation>,
    ): ResponseEntity<DataService> =
        handler
            .update(catalogId, dataServiceId, operations)
            .let { ResponseEntity.ok(it) }

    @PreAuthorize(WRITE)
    @PostMapping("/{dataServiceId}/publish")
    fun publishDataService(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
    ): ResponseEntity<Void> =
        handler
            .publish(catalogId, dataServiceId)
            .let { ResponseEntity.ok().build() }

    @PreAuthorize(WRITE)
    @PostMapping("/{dataServiceId}/unpublish")
    fun unpublishDataService(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
    ): ResponseEntity<Void> =
        handler
            .unpublish(catalogId, dataServiceId)
            .let { ResponseEntity.ok().build() }

    @PreAuthorize(WRITE)
    @DeleteMapping("/{dataServiceId}")
    fun deleteDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
    ): ResponseEntity<Void> =
        handler
            .delete(catalogId, dataServiceId)
            .let {
                ResponseEntity
                    .noContent()
                    .build()
            }

    @ExceptionHandler
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to validate content.")

        ex.bindingResult.fieldErrors
            .map { fieldError ->
                mapOf(
                    "field" to fieldError.field,
                    "message" to fieldError.defaultMessage,
                )
            }.also { problemDetail.setProperty("errors", it) }

        return ResponseEntity.of(problemDetail).build()
    }

    @ExceptionHandler
    fun handleMethodArgumentNotValidException(ex: JsonProcessingException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.originalMessage)

        return ResponseEntity.of(problemDetail).build()
    }

    @ExceptionHandler
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ProblemDetail> =
        ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)).build()

    @ExceptionHandler
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ProblemDetail> =
        ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message)).build()

    @ExceptionHandler
    fun handleInternalServerErrorException(ex: InternalServerErrorException): ResponseEntity<ProblemDetail> =
        ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)).build()

    companion object {
        const val READ =
            "hasAnyAuthority('system:root:admin', 'organization:' + #catalogId + ':admin', 'organization:' + #catalogId + ':write', 'organization:' + #catalogId + ':read')"

        const val WRITE =
            "hasAnyAuthority('organization:' + #catalogId + ':admin', 'organization:' + #catalogId + ':write')"
    }
}
