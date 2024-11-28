package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.handler.RDFHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import java.io.StringReader

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
    fun `find should respond with turtle rdf`() {
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

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, "TURTLE");

        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn listOf(dataService(dataServiceId, catalogId))
        }

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(handler.findAll(Lang.TURTLE)), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find by id should respond with empty string`() {
        val catalogId = "1234"

        repository.stub {
            on { findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED) } doReturn emptyList()
        }

        assertEquals("", handler.findById(catalogId, Lang.TURTLE))
    }

    @Test
    fun `find by id should respond with turtle rdf`() {
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

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, "TURTLE")

        repository.stub {
            on { findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED) } doReturn listOf(
                dataService(
                    dataServiceId,
                    catalogId
                )
            )
        }

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(handler.findById(catalogId, Lang.TURTLE)), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find data service by id should throw exception`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId, catalogId = "invalid catalog id"
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.findById(catalogId, dataServiceId, Lang.TURTLE)
        }
    }

    @Test
    fun `find data service by id should respond with turtle rdf`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
            
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
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn dataService(dataServiceId, catalogId)
        }

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(handler.findById(catalogId, dataServiceId, Lang.TURTLE)), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    private fun dataService(id: String, catalogId: String) = DataService(
        id = id,
        catalogId = catalogId,
        endpointUrl = "http://example.com",
        titles = listOf(LanguageString("en", "title")),
        keywords = listOf(LanguageString("en", "keyword")),
        endpointDescriptions = listOf("http://endpoint-description.com"),
        formats = listOf("http://format.com"),
        contactPoint = ContactPoint(
            name = "name", phone = "phone", email = "email", url = "url"
        ),
        servesDataset = listOf("http://serves-dataset.com"),
        description = LanguageString("en", "description"),
        pages = listOf("http://page.com"),
        landingPage = "http://landing-page.com",
        license = License(
            name = "name", url = "http://license.com"
        ),
        mediaTypes = listOf(
            "https://www.iana.org/assignments/media-types/application/json", "application/xml"
        ),
        accessRights = "http://access-rights.com",
        type = "http://type.com"
    )
}
