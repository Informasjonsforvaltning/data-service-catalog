package no.fdk.dataservicecatalog.controller

import jakarta.validation.Valid
import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.PatchRequest
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
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/internal/catalogs/{catalogId}/data-services")
class DataServiceController(private val handler: DataServiceHandler) {

    @PreAuthorize(READ)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServicesByCatalogId(@PathVariable catalogId: String): ResponseEntity<List<DataService>> {
        return handler.findAll(catalogId)
            .let { ResponseEntity.ok(it) }
    }

    @PreAuthorize(READ)
    @GetMapping("/{dataServiceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<DataService> {
        return handler.findById(catalogId, dataServiceId)
            .let { ResponseEntity.ok(it) }
    }

    @PreAuthorize(WRITE)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun registerDataServiceByCatalogId(
        @PathVariable catalogId: String, @Valid @RequestBody dataService: DataService
    ): ResponseEntity<Void> {
        return handler.register(catalogId, dataService)
            .let {
                ResponseEntity
                    .created(URI("/internal/catalogs/${catalogId}/data-services/${it}"))
                    .build()
            }
    }

    @PreAuthorize(WRITE)
    @PatchMapping(
        "/{dataServiceId}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String,
        @PathVariable dataServiceId: String,
        @Valid @RequestBody patchRequest: PatchRequest
    ): ResponseEntity<DataService> {
        return handler.update(catalogId, dataServiceId, patchRequest)
            .let { ResponseEntity.ok(it) }
    }

    @PreAuthorize(WRITE)
    @DeleteMapping("/{dataServiceId}")
    fun deleteDataServiceByCatalogIdAndDataServiceId(
        @PathVariable catalogId: String, @PathVariable dataServiceId: String
    ): ResponseEntity<Void> {
        return handler.delete(catalogId, dataServiceId)
            .let {
                ResponseEntity
                    .noContent()
                    .build()
            }
    }

    @ExceptionHandler
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val problemDetail = ex.body

        ex.bindingResult.fieldErrors.map { fieldError ->
            mapOf(
                "field" to fieldError.field,
                "message" to fieldError.defaultMessage
            )
        }.also { problemDetail.setProperty("errors", it) }

        return ResponseEntity.of(problemDetail).build()
    }

    @ExceptionHandler
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)).build()
    }

    @ExceptionHandler
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message)).build()
    }

    @ExceptionHandler
    fun handleInternalServerErrorException(ex: InternalServerErrorException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)).build()
    }

    companion object {
        const val READ =
            "hasAnyAuthority('system:root:admin', 'organization:' + #catalogId + ':admin', 'organization:' + #catalogId + ':write', 'organization:' + #catalogId + ':read')"

        const val WRITE =
            "hasAnyAuthority('organization:' + #catalogId + ':admin', 'organization:' + #catalogId + ':write')"
    }
}
