package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.entity.DataServiceEntity
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
                DataServiceEntity(
                    id = "5678",
                    catalogId = catalogId,
                    published = true,
                    data = mapOf(
                        Pair("endpointUrl", "endpointUrl"),
                        Pair("title", LocalizedStrings(nb = "title"))
                    )
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
            on { findDataServiceById(dataServiceId) } doReturn DataServiceEntity(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                data = mapOf(
                    Pair("endpointUrl", "endpointUrl"),
                    Pair("title", LocalizedStrings(nb = "title"))
                )
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
            on { findDataServiceById(dataServiceId) } doReturn DataServiceEntity(
                id = dataServiceId,
                catalogId = "invalid_catalog id",
                published = true,
                data = mapOf(
                    Pair("endpointUrl", "endpointUrl"),
                    Pair("title", LocalizedStrings(nb = "title"))
                )
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
            catalogId, DataServiceValues(
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title"),
                status = null,
                keywords = null,
                endpointDescriptions = null,
                formats = null,
                contactPoint = null,
                themes = null,
                servesDataset = null,
                description = null,
                pages = null,
                landingPage = null,
                license = null,
                mediaTypes = null,
                accessRights = null,
                type = null,
                availability = null,
                costs = null
            )
        )

        assertTrue(dataServiceId.isNotBlank())

        verify(repository).save(any<DataServiceEntity>())
    }

    @Test
    fun `should update data service`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        val dataService = DataServiceEntity(
            id = dataServiceId,
            catalogId = catalogId,
            published = true,
            data = mapOf(
                Pair("endpointUrl", "endpointUrl")
            )
        )

        val patchedDataService = dataService.copy(
            data = mapOf(
                Pair("endpointUrl", "newEndpointUrl")
            )
        )

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn dataService
            on { save(patchedDataService) } doReturn patchedDataService
        }

        val operations = listOf(
            JsonPatchOperation(
                op = OpEnum.REPLACE,
                path = "/endpointUrl",
                value = "newEndpointUrl"
            )
        )

        val update = handler.update(catalogId, dataServiceId, operations)

        assertEquals("newEndpointUrl", update.endpointUrl)
    }

    @Test
    fun `update throws exception on invalid catalog id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataServiceEntity(
                id = dataServiceId,
                catalogId = "invalid catalog id",
                published = true,
                data = mapOf(
                    Pair("endpointUrl", "endpointUrl"),
                    Pair("title", LocalizedStrings(nb = "title"))
                )
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.update(
                catalogId, dataServiceId, listOf(
                    JsonPatchOperation(
                        op = OpEnum.REPLACE,
                        path = "/endpointUrl",
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
            on { findDataServiceById(dataServiceId) } doReturn DataServiceEntity(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                data = mapOf(
                    Pair("endpointUrl", "endpointUrl"),
                    Pair("title", LocalizedStrings(nb = "title"))
                )
            )
        }

        handler.delete(catalogId, dataServiceId)

        verify(repository).delete(any<DataServiceEntity>())
    }

    @Test
    fun `delete throws exception on invalid catalog id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataServiceEntity(
                id = dataServiceId,
                catalogId = "invalid catalog id",
                published = true,
                data = mapOf(
                    Pair("endpointUrl", "endpointUrl"),
                    Pair("title", LocalizedStrings(nb = "title"))
                )
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.delete(catalogId, dataServiceId)
        }
    }
}
