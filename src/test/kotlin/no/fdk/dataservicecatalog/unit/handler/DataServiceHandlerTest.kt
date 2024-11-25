package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@ExtendWith(MockitoExtension::class)
class DataServiceHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: DataServiceHandler

    @Test
    fun `find all returns list of one`() {
        repository.stub {
            on { repository.existsByCatalogId("1234") } doReturn true
            on { repository.findAllByCatalogIdOrderByCreatedDesc(catalogId = "1234") } doReturn listOf(
                DataService(
                    catalogId = "1234"
                )
            )
        }

        assertEquals(1, handler.findAll("1234").size)
    }

    @Test
    fun `find by id returns data service`() {
        repository.stub {
            on { repository.existsByCatalogId("1234") } doReturn true
            on { repository.findByCatalogIdAndId("1234", "5678") } doReturn DataService(catalogId = "1234")
        }

        assertNotNull(handler.findById("1234", "5678"))
    }

    @Test
    fun `should delete data service`() {
        repository.stub {
            on { repository.existsByCatalogId("1234") } doReturn true
            on { repository.existsById("5678") } doReturn true
        }

        handler.delete("1234", "5678")
    }
}
