package no.fdk.dataservicecatalog.adapter

import no.fdk.dataservicecatalog.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.net.URI

@Service
class HarvestAdminClient(
    private val applicationProperties: ApplicationProperties,
    private val restTemplate: RestTemplate = RestTemplate(),
) {

    fun createNewDataSource(catalogId: String) {
        val baseUri = applicationProperties.harvestAdminUri

        val url = "$baseUri/organizations/$catalogId/datasources"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            resolveBearerToken()?.let { set(HttpHeaders.AUTHORIZATION, "Bearer $it") }
        }

        val body = HarvestAdminDataSource(
            dataSourceType = "DCAT-AP-NO",
            dataType = "dataservice",
            url = "${applicationProperties.dataServiceCatalogUriHost}/$catalogId",
            acceptHeaderValue = "text/turtle",
            publisherId = catalogId,
            description = "Automatically generated data source for $catalogId"
        )

        runCatching {
            restTemplate.postForEntity<Any>(URI(url), HttpEntity(body, headers))
        }.onFailure {
            logger.error("Error calling Harvest Admin createDataSource for catalog {}", catalogId, it)
        }
    }

    fun triggerHarvest(catalogId: String) {
        val baseUri = applicationProperties.harvestAdminUri

        val url = "$baseUri/organizations/$catalogId/datasources/start-harvesting"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            resolveBearerToken()?.let { set(HttpHeaders.AUTHORIZATION, "Bearer $it") }
        }

        val body = StartHarvestByUrlRequest(
            url = "${applicationProperties.dataServiceCatalogUriHost}/$catalogId",
            dataType = "dataservice",
        )

        runCatching {
            restTemplate.postForEntity<Any>(URI(url), HttpEntity(body, headers))
        }.onFailure {
            logger.error("Error calling Harvest Admin startHarvestingByUrlAndDataType for catalog {}", catalogId, it)
        }
    }

    private fun resolveBearerToken(): String? =
        (SecurityContextHolder.getContext().authentication?.principal as? Jwt)
            ?.tokenValue

    companion object {
        private val logger = LoggerFactory.getLogger(HarvestAdminClient::class.java)
    }
}

private data class HarvestAdminDataSource(
    val dataSourceType: String,
    val dataType: String,
    val url: String,
    val acceptHeaderValue: String? = null,
    val publisherId: String,
    val description: String? = null,
)

data class StartHarvestByUrlRequest(
    val url: String,
    val dataType: String,
)
