package no.fdk.dataservicecatalog.unit.handler

import no.fdk.dataservicecatalog.ApplicationProperties
import no.fdk.dataservicecatalog.domain.*
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.handler.RDFHandler
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Mock
    lateinit var properties: ApplicationProperties

    @InjectMocks
    lateinit var handler: RDFHandler

    @Test
    fun `find should respond with empty turtle rdf`() {
        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn emptyList()
        }

        val catalogs = handler.findCatalogs(Lang.TURTLE)

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(catalogs), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find should respond with turtle rdf`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        val baseUri = "http://base-uri.com"
        val organizationCatalogBaseUri = "http://organization-catalog-base-uri.com"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>

            <$organizationCatalogBaseUri/organizations/$catalogId>
                    rdf:type        foaf:Agent;
                    dct:identifier  "$catalogId";
                    <http://www.w3.org/2002/07/owl#sameAs>
                            "https://data.brreg.no/enhetsregisteret/api/enheter/$catalogId" .
            
            <$baseUri/catalogs/$catalogId/data-services/$dataServiceId>
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
                    dcat:mediaType            <https://www.iana.org/assignments/media-types/application/json>;
                    dcat:servesDataset        <http://serves-dataset.com>;
                    foaf:page                 <http://page.com> .

            <$baseUri/catalogs/$catalogId>  rdf:type  dcat:Catalog;
                    dct:publisher  <$organizationCatalogBaseUri/organizations/$catalogId>;
                    dct:title      "Data service catalog ($catalogId)"@en;
                    dcat:service   <$baseUri/catalogs/$catalogId/data-services/$dataServiceId> .
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findAllByStatus(Status.PUBLISHED) } doReturn listOf(
                dataService().copy(
                    id = dataServiceId,
                    catalogId = catalogId
                )
            )
        }

        properties.stub {
            on { this.baseUri } doReturn baseUri
            on { this.organizationCatalogBaseUri } doReturn organizationCatalogBaseUri
        }

        val catalogs = handler.findCatalogs(Lang.TURTLE)

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(catalogs), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find by id should respond with empty turtle rdf`() {
        val catalogId = "1234"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED) } doReturn emptyList()
        }

        val catalogs = handler.findCatalogById(catalogId, Lang.TURTLE)

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(catalogs), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find by id should respond with turtle rdf`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        val baseUri = "http://base-uri.com"
        val organizationCatalogBaseUri = "http://organization-catalog-base-uri.com"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>

            <$organizationCatalogBaseUri/organizations/$catalogId>
                    rdf:type        foaf:Agent;
                    dct:identifier  "$catalogId";
                    <http://www.w3.org/2002/07/owl#sameAs>
                            "https://data.brreg.no/enhetsregisteret/api/enheter/$catalogId" .
            
            <$baseUri/catalogs/$catalogId/data-services/$dataServiceId>
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
                    dcat:mediaType            <https://www.iana.org/assignments/media-types/application/json>;
                    dcat:servesDataset        <http://serves-dataset.com>;
                    foaf:page                 <http://page.com> .

            <$baseUri/catalogs/$catalogId>  rdf:type  dcat:Catalog;
                    dct:publisher  <$organizationCatalogBaseUri/organizations/$catalogId>;
                    dct:title      "Data service catalog ($catalogId)"@en;
                    dcat:service   <$baseUri/catalogs/$catalogId/data-services/$dataServiceId> .
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED) } doReturn listOf(
                dataService().copy(
                    id = dataServiceId,
                    catalogId = catalogId
                )
            )
        }

        properties.stub {
            on { this.baseUri } doReturn baseUri
            on { this.organizationCatalogBaseUri } doReturn organizationCatalogBaseUri
        }

        val catalogs = handler.findCatalogById(catalogId, Lang.TURTLE)

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(catalogs), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    @Test
    fun `find data service by id should throw exception`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn DataService(
                id = dataServiceId,
                catalogId = "invalid catalog id",
                status = Status.PUBLISHED,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        }

        assertThrows(NotFoundException::class.java) {
            handler.findDataServiceByCatalogIdAndDataServiceId(catalogId, dataServiceId, Lang.TURTLE)
        }
    }

    @Test
    fun `find data service by id should respond with turtle rdf`() {
        val dataServiceId = "1234"
        val catalogId = "5678"

        val baseUri = "http://base-uri.com"

        val rdf = """
            PREFIX dcat:  <http://www.w3.org/ns/dcat#>
            PREFIX dct:   <http://purl.org/dc/terms/>
            PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
            PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
            
            <$baseUri/catalogs/$catalogId/data-services/$dataServiceId>
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
                    dcat:mediaType            <https://www.iana.org/assignments/media-types/application/json>;
                    dcat:servesDataset        <http://serves-dataset.com>;
                    foaf:page                 <http://page.com> .
        """

        val expectedModel = ModelFactory.createDefaultModel()
        expectedModel.read(StringReader(rdf), null, Lang.TURTLE.name)

        repository.stub {
            on { findDataServiceById(dataServiceId) } doReturn dataService().copy(
                id = dataServiceId,
                catalogId = catalogId
            )
        }

        properties.stub {
            on { this.baseUri } doReturn baseUri
        }

        val dataService = handler.findDataServiceByCatalogIdAndDataServiceId(catalogId, dataServiceId, Lang.TURTLE)

        val actualModel = ModelFactory.createDefaultModel()
        actualModel.read(StringReader(dataService), null, Lang.TURTLE.name)

        assertTrue(expectedModel.isIsomorphicWith(actualModel))
    }

    private fun dataService() = DataService(
        id = "1234",
        catalogId = "5678",
        status = Status.PUBLISHED,
        endpointUrl = "http://example.com",
        title = LocalizedStrings(en = "title"),
        keywords = listOf(LocalizedStrings(en = "keyword")),
        endpointDescriptions = listOf("http://endpoint-description.com"),
        formats = listOf("http://format.com"),
        contactPoint = ContactPoint(
            organizationUnit = "name",
            phone = "phone",
            email = "email",
            url = "url"
        ),
        servesDataset = listOf("http://serves-dataset.com"),
        description = LocalizedStrings(en = "description"),
        pages = listOf("http://page.com"),
        landingPage = "http://landing-page.com",
        license = License(
            name = "name",
            url = "http://license.com"
        ),
        mediaTypes = listOf("https://www.iana.org/assignments/media-types/application/json"),
        accessRights = "http://access-rights.com",
        type = "http://type.com"
    )
}
