package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.handler.DataServiceHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class DataServiceHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: DataServiceHandler

    @Test
    fun `find should respond with empty list`() {
        assertTrue(handler.findAll("1234").isEmpty())
    }

    @Test
    fun `find by id should respond with data service`() {
        assertEquals(DataService("5678"), handler.findById("1234", "5678"))
    }
}