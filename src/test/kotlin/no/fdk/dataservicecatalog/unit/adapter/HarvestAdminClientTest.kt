package no.fdk.dataservicecatalog.unit.adapter

import no.fdk.dataservicecatalog.ApplicationProperties
import no.fdk.dataservicecatalog.adapter.HarvestAdminClient
import no.fdk.dataservicecatalog.adapter.StartHarvestByUrlRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate
import java.net.URI

@Tag("unit")
@ExtendWith(MockitoExtension::class)
class HarvestAdminClientTest {

    private val baseUri = "https://harvest-admin.example.com"
    private val catalogId = "1234"
    private val dataServiceCatalogHost = "https://dataservice.example.com"

    @Mock
    lateinit var restTemplate: RestTemplate

    @Mock
    lateinit var applicationProperties: ApplicationProperties

    lateinit var client: HarvestAdminClient

    @BeforeEach
    fun setUp() {
        client = HarvestAdminClient(applicationProperties, restTemplate)
    }

    @Captor
    lateinit var uriCaptor: ArgumentCaptor<URI>

    @Captor
    lateinit var httpEntityCaptor: ArgumentCaptor<HttpEntity<Any>>

    @Test
    fun `createNewDataSource posts expected payload to Harvest Admin`() {
        org.mockito.kotlin.whenever(applicationProperties.harvestAdminUri).thenReturn(baseUri)
        org.mockito.kotlin.whenever(applicationProperties.dataServiceCatalogUriHost).thenReturn(dataServiceCatalogHost)

        client.createNewDataSource(catalogId)

        verify(restTemplate).postForEntity(
            capture(uriCaptor),
            capture(httpEntityCaptor),
            eq(Any::class.java)
        )

        val capturedUri = uriCaptor.value
        val capturedEntity = httpEntityCaptor.value

        assert(capturedUri.toString() == "$baseUri/organizations/$catalogId/datasources")

        val bodyString = capturedEntity.body.toString()

        assert(bodyString.contains("dataSourceType=DCAT-AP-NO"))
        assert(bodyString.contains("dataType=dataservice"))
        assert(bodyString.contains("url=$dataServiceCatalogHost/$catalogId"))
        assert(bodyString.contains("acceptHeaderValue=text/turtle"))
        assert(bodyString.contains("publisherId=$catalogId"))
        assert(bodyString.contains(catalogId))

        val headers: HttpHeaders = capturedEntity.headers
        assert(headers.contentType?.toString() == "application/json")
    }

    @Test
    fun `triggerHarvest posts expected payload to Harvest Admin`() {
        org.mockito.kotlin.whenever(applicationProperties.harvestAdminUri).thenReturn(baseUri)
        org.mockito.kotlin.whenever(applicationProperties.dataServiceCatalogUriHost).thenReturn(dataServiceCatalogHost)

        client.triggerHarvest(catalogId)

        verify(restTemplate).postForEntity(
            eq(URI("$baseUri/organizations/$catalogId/datasources/start-harvesting")),
            any<HttpEntity<StartHarvestByUrlRequest>>(),
            eq(Any::class.java)
        )
    }

    // Helper to use ArgumentCaptor with Mockito-Kotlin capture syntax
    private fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()
}

