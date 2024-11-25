package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import java.util.*

@ExtendWith(MockitoExtension::class)
class DataServiceHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: DataServiceHandler

    @Test
    fun `find all returns list of one`() {
        repository.stub {
            on { existsByCatalogId("1234") } doReturn true
            on { findAllByCatalogIdOrderByCreatedDesc(catalogId = "1234") } doReturn listOf(
                DataService(
                    catalogId = "1234"
                )
            )
        }

        val dataServices = handler.findAll("1234")

        assertEquals(1, dataServices.size)
    }

    @Test
    fun `find by id returns data service`() {
        repository.stub {
            on { existsByCatalogId("1234") } doReturn true
            on { findByCatalogIdAndId("1234", "5678") } doReturn DataService(catalogId = "1234")
        }

        val dataService = handler.findById("1234", "5678")

        assertNotNull(dataService)
    }

    @Test
    fun `should register data service and return id`() {
        repository.stub {
            on { existsByCatalogId("1234") } doReturn true
        }

        assertDoesNotThrow {
            val dataServiceId = handler.register("1234", DataService(catalogId = "1234"))
            assertTrue(dataServiceId.isNotBlank())

            UUID.fromString(dataServiceId)
        }
    }

    @Test
    fun `should delete data service`() {
        repository.stub {
            on { existsByCatalogId("1234") } doReturn true
            on { existsById("5678") } doReturn true
        }

        assertDoesNotThrow {
            handler.delete("1234", "5678")
        }
    }
}
