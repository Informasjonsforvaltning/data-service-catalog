package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.JsonPatchOperation
import no.fdk.dataservicecatalog.domain.OpEnum
import no.fdk.dataservicecatalog.domain.PatchRequest
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class DataServiceHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: DataServiceHandler

    @Test
    fun `find all returns list of one`() {
        val catalogId = "1234"

        repository.stub {
            on { existsByCatalogId(catalogId) } doReturn true
            on { findAllByCatalogIdOrderByCreatedDesc(catalogId) } doReturn listOf(
                DataService(catalogId = catalogId)
            )
        }

        val dataServices = handler.findAll(catalogId)

        assertEquals(1, dataServices.size)
    }

    @Test
    fun `find by id returns data service`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { existsByCatalogId(catalogId) } doReturn true
            on { findByCatalogIdAndId(catalogId, dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = catalogId
            )
        }

        val dataService = handler.findById(catalogId, dataServiceId)

        assertEquals(dataServiceId, dataService.id)
    }

    @Test
    fun `should register data service and return id`() {
        val catalogId = "1234"

        val dataServiceId = handler.register(catalogId, DataService())

        assertTrue(dataServiceId.isNotBlank())

        verify(repository).insert(any<DataService>())
    }

    @Test
    fun `should update data service`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        val dataService = DataService(
            id = dataServiceId,
            catalogId = catalogId,
            endpointUrl = "endpointUrl"
        )

        val patchedDataService = dataService.copy(
            endpointUrl = "newEndpointUrl"
        )

        repository.stub {
            on { existsByCatalogId(catalogId) } doReturn true
            on { findByCatalogIdAndId(catalogId, dataServiceId) } doReturn dataService
            on { save(patchedDataService) } doReturn patchedDataService
        }

        val patchRequest = PatchRequest(
            listOf(
                JsonPatchOperation(
                    op = OpEnum.REPLACE,
                    path = "/endpointUrl",
                    value = "newEndpointUrl"
                )
            )
        )

        val update = handler.update(catalogId, dataServiceId, patchRequest)

        assertEquals("newEndpointUrl", update.endpointUrl)
    }

    @Test
    fun `should delete data service`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { existsByCatalogId(catalogId) } doReturn true
            on { existsById(dataServiceId) } doReturn true
        }

        handler.delete(catalogId, dataServiceId)

        verify(repository).deleteById(dataServiceId)
    }
}
