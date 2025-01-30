package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.LocalizedStrings
import no.fdk.dataservicecatalog.handler.CountHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@ExtendWith(MockitoExtension::class)
class CountHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: CountHandler

    @Test
    fun `find all returns list of data service counts`() {
        val firstCatalogId = "1234"
        val secondCatalogId = "5678"

        val dataService = DataService(
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        repository.stub {
            on { findAll() } doReturn listOf(
                dataService,
                dataService.copy(id = "2222"),
                dataService.copy(id = "3333", catalogId = secondCatalogId)
            )
        }

        val dataServiceCounts = handler.findAll()

        assertEquals(2, dataServiceCounts.count())

        val firstDataServiceCount = dataServiceCounts.first()
        assertEquals(firstCatalogId, firstDataServiceCount.catalogId)
        assertEquals(2, firstDataServiceCount.dataServiceCount)

        val lastDataServiceCount = dataServiceCounts.last()
        assertEquals(secondCatalogId, lastDataServiceCount.catalogId)
        assertEquals(1, lastDataServiceCount.dataServiceCount)
    }


    @Test
    fun `find selected returns list of data service`() {
        val firstCatalogId = "1234"
        val secondCatalogId = "5678"

        val dataService = DataService(
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        repository.stub {
            on { findAllByCatalogIdIn(setOf(firstCatalogId, secondCatalogId)) } doReturn listOf(
                dataService,
                dataService.copy(id = "2222"),
                dataService.copy(id = "3333", catalogId = secondCatalogId)
            )
        }

        val dataServiceCounts = handler.findSelected(setOf(firstCatalogId, secondCatalogId))

        assertEquals(2, dataServiceCounts.count())

        val firstDataServiceCount = dataServiceCounts.first()
        assertEquals(firstCatalogId, firstDataServiceCount.catalogId)
        assertEquals(2, firstDataServiceCount.dataServiceCount)

        val lastDataServiceCount = dataServiceCounts.last()
        assertEquals(secondCatalogId, lastDataServiceCount.catalogId)
        assertEquals(1, lastDataServiceCount.dataServiceCount)
    }
}
