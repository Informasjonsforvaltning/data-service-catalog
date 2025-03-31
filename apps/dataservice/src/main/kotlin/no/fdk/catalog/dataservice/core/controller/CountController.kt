package no.fdk.catalog.dataservice.core.controller

import no.fdk.catalog.dataservice.core.domain.DataServiceCount
import no.fdk.catalog.dataservice.core.handler.CountHandler
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/catalogs/count")
class CountController(private val handler: CountHandler) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findDataServicesCount(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<DataServiceCount>> {
        val authorities: String? = jwt.claims["authorities"] as? String

        return if (authorities?.contains("system:root:admin") == true) {
            ResponseEntity.ok(handler.findAll())
        } else {
            val regex = Regex("""[0-9]{9}""")

            val ids = (authorities
                ?.let { regex.findAll(it) }
                ?.map { matchResult -> matchResult.value }
                ?.toSet()
                ?: emptySet())

            ResponseEntity.ok(handler.findSelected(ids))
        }
    }
}
