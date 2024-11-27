package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.handler.RDFHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

@ExtendWith(MockitoExtension::class)
class RDFHandlerTest {

    @Mock
    lateinit var repository: DataServiceRepository

    @InjectMocks
    lateinit var handler: RDFHandler

    @Test
    fun `find should respond with empty string`() {
        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn emptyList()
        }

        assertEquals("", handler.findAll(Lang.TURTLE))
    }

    @Test
    fun `find should respond with turtle string`() {
        val turtle = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
            
            </catalogs/1234>  rdf:type  dcat:Catalog;
                    dct:publisher  <1234>;
                    dct:title      "Data service catalog (1234)"@en .

            </organizations/1234>
                    rdf:type        foaf:Agent;
                    dct:identifier  "1234";
                    <http://www.w3.org/2002/07/owl#sameAs>
                            "1234" .
        """

        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn listOf(DataService(catalogId = "1234"))
        }

        assertEquals(turtle.trimIndent(), handler.findAll(Lang.TURTLE).trimEnd())
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
