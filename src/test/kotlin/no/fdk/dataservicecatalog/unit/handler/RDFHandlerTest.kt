package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.*
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
        val dataServiceId = "1234"
        val catalogId = "5678"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>

            </organizations/$catalogId>
                    rdf:type        foaf:Agent;
                    dct:identifier  "$catalogId";
                    <http://www.w3.org/2002/07/owl#sameAs>
                            "$catalogId" .
            
            </data-services/$dataServiceId>
                    rdf:type                  dcat:DataService;
                    dct:accessRights          <http://access-rights.com>;
                    dct:description           "description"@en;
                    dct:format                <http://format.com>;
                    dct:license               <http://license.com>;
                    dct:title                 "title"@en;
                    dct:type                  <http://type.com>;
                    dcat:contactPoint         [ rdf:type                   vcard:Organization;
                                                vcard:fn                   "Contact information | (5678)";
                                                vcard:hasEmail             <mailto:email>;
                                                vcard:hasOrganizationName  "name"@nb;
                                                vcard:hasTelephone         [ rdf:type        vcard:TelephoneType;
                                                                             vcard:hasValue  <tel:phone>
                                                                           ];
                                                vcard:hasURL               <url>
                                              ];
                    dcat:endpointDescription  <http://endpoint-description.com>;
                    dcat:endpointURL          <http://example.com>;
                    dcat:keyword              "keyword"@en;
                    dcat:landingPage          <http://landing-page.com>;
                    dcat:mediaType            <https://www.iana.org/assignments/media-types/application/xml> , <https://www.iana.org/assignments/media-types/application/json>;
                    dcat:servesDataset        <http://serves-dataset.com>;
                    foaf:page                 <http://page.com> .

            </catalogs/$catalogId>  rdf:type  dcat:Catalog;
                    dct:publisher  <$catalogId>;
                    dct:title      "Data service catalog ($catalogId)"@en;
                    dcat:service   </data-services/$dataServiceId> .
        """

        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn listOf(
                DataService(
                    id = "1234",
                    catalogId = "5678",
                    endpointUrl = "http://example.com",
                    titles = listOf(LanguageString("en", "title")),
                    keywords = listOf(LanguageString("en", "keyword")),
                    endpointDescriptions = listOf("http://endpoint-description.com"),
                    formats = listOf("http://format.com"),
                    contactPoint = ContactPoint(
                        name = "name",
                        phone = "phone",
                        email = "email",
                        url = "url"
                    ),
                    servesDataset = listOf("http://serves-dataset.com"),
                    description = LanguageString("en", "description"),
                    pages = listOf("http://page.com"),
                    landingPage = "http://landing-page.com",
                    license = License(
                        name = "name",
                        url = "http://license.com"
                    ),
                    mediaTypes = listOf(
                        "https://www.iana.org/assignments/media-types/application/json",
                        "application/xml"
                    ),
                    accessRights = "http://access-rights.com",
                    type = "http://type.com"
                )
            )
        }

        assertEquals(rdf.trimIndent(), handler.findAll(Lang.TURTLE).trimEnd())
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
