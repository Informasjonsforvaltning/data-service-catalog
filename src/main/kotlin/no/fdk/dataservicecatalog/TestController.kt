package no.fdk.dataservicecatalog

import org.springframework.http.HttpStatus.NOT_IMPLEMENTED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): ResponseEntity<Void> {
        return ResponseEntity.status(NOT_IMPLEMENTED).build()
    }
}
