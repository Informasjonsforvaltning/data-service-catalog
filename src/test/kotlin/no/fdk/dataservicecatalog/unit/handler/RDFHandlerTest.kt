package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.handler.RDFHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RDFHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: RDFHandler

    @Test
    fun `find should respond with empty string`() {
        assertEquals("", handler.findAll(Lang.TURTLE))
    }

    @Test
    fun `find by id should respond with empty string`() {
        assertEquals("", handler.findById("1234", Lang.TURTLE))
    }

    @Test
    fun `find data service by id should respond with empty string`() {
        assertEquals("", handler.findById("1234", "5678", Lang.TURTLE))
    }
}
