package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@Tag("unit")
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
            on { findAllByCatalogId(catalogId) } doReturn listOf(
                DataService(
                    id = "5678",
                    catalogId = catalogId,
                    published = true,
                    status = null,
                    endpointUrl = "endpointUrl",
                    title = LocalizedStrings(nb = "title")
                )
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
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        val dataService = handler.findById(catalogId, dataServiceId)

        assertEquals(dataServiceId, dataService.id)
    }

    @Test
    fun `find by id throws exception on invalid catalog id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = "invalid_catalog id",
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.findById(catalogId, dataServiceId)
        }
    }

    @Test
    fun `should register data service and return id`() {
        val catalogId = "1234"

        val dataServiceId = handler.register(
            catalogId, RegisterDataService(
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        )

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
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        val patchedDataService = dataService.copy(
            endpointUrl = "newEndpointUrl"
        )

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn dataService
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
    fun `update throws exception on invalid catalog id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = "invalid catalog id",
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.update(
                catalogId, dataServiceId, PatchRequest(
                    patchOperations = listOf(
                        JsonPatchOperation(
                            op = OpEnum.REPLACE,
                            path = "/endpointUrl",
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should delete data service`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        handler.delete(catalogId, dataServiceId)

        verify(repository).delete(any<DataService>())
    }

    @Test
    fun `delete throws exception on invalid catalog id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = "invalid catalog id",
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.delete(catalogId, dataServiceId)
        }
    }
}
