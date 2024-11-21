package no.fdk.dataservicecatalog.controller

import no.fdk.dataservicecatalog.domain.DataServiceCount
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/catalogs/count", produces = [MediaType.APPLICATION_JSON_VALUE])
class CountController {

    @GetMapping
    fun getDataServicesCount(): ResponseEntity<List<DataServiceCount>> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }
}
