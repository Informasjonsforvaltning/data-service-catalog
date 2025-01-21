package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.ApplicationProperties
import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.util.FileUtils
import org.apache.jena.util.URIref
import org.apache.jena.vocabulary.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.StringWriter

@Component
class RDFHandler(private val repository: DataServiceRepository, private val properties: ApplicationProperties) {

    fun findCatalogs(lang: Lang): String {
        val model = createModel()

        repository.findAllByStatus(Status.PUBLISHED)
            .groupBy(DataService::catalogId)
            .forEach { (catalogId, dataServices) ->
                catalogId.let { id ->
                    model.addCatalog(id, getCatalogUri(), getOrganizationUri(), getPublisherUri())

                    dataServices.forEach { dataService ->
                        model.addDataServiceToCatalog(dataService, getCatalogUri(), getDataServiceUri(id))
                        model.addDataService(dataService, getDataServiceUri(id))
                    }
                }
            }

        return model.serialize(lang)
    }

    fun findCatalogById(catalogId: String, lang: Lang): String {
        val model = createModel()

        repository.findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED)
            .forEach { dataService ->
                dataService.catalogId.let { id ->
                    model.addCatalog(id, getCatalogUri(), getOrganizationUri(), getPublisherUri())
                    model.addDataServiceToCatalog(dataService, getCatalogUri(), getDataServiceUri(id))
                    model.addDataService(dataService, getDataServiceUri(id))
                }
            }

        return model.serialize(lang)
    }

    fun findDataServiceByCatalogIdAndDataServiceId(catalogId: String, dataServiceId: String, lang: Lang): String {
        val dataService = repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")

        val model = createModel()

        dataService.catalogId
            .let { id ->
                model.addDataService(dataService, getDataServiceUri(id))
            }

        return model.serialize(lang)
    }

    private fun createModel(): Model {
        return ModelFactory.createDefaultModel()
            .apply {
                setNsPrefixes(
                    mapOf(
                        "dcat" to DCAT.NS,
                        "dct" to DCTerms.NS,
                        "rdf" to RDF.uri,
                        "vcard" to VCARD4.NS,
                        "foaf" to FOAF.NS
                    )
                )
            }
    }

    private fun getCatalogUri(): String {
        return buildUri(properties.baseUri, "/catalogs/")
    }

    private fun getDataServiceUri(catalogId: String): String {
        return buildUri(properties.baseUri, "/catalogs/$catalogId/data-services/")
    }

    private fun getOrganizationUri(): String {
        return buildUri(properties.organizationCatalogBaseUri, "/organizations/")
    }

    private fun buildUri(baseUri: String, path: String): String {
        return "$baseUri$path"
    }

    private fun getPublisherUri(): String {
        return "https://data.brreg.no/enhetsregisteret/api/enheter/"
    }
}

fun Model.addCatalog(catalogId: String, baseUri: String, organizationCatalogUri: String, publisherUri: String) {
    this.createResource(URIref.encode(baseUri.plus(catalogId))).addProperty(
        RDF.type, DCAT.Catalog
    ).addProperty(
        DCTerms.publisher, ResourceFactory.createResource(URIref.encode(organizationCatalogUri.plus(catalogId)))
    ).addProperty(
        DCTerms.title, ResourceFactory.createLangLiteral("Data service catalog ($catalogId)", "en")
    )

    this.createResource(URIref.encode(organizationCatalogUri.plus(catalogId))).addProperty(
        RDF.type, FOAF.Agent
    ).addProperty(
        DCTerms.identifier, catalogId
    ).addProperty(
        OWL.sameAs, URIref.encode(publisherUri.plus(catalogId))
    )
}

fun Model.addDataServiceToCatalog(dataService: DataService, catalogUri: String, dataServiceUri: String) {
    this.getProperty(URIref.encode(catalogUri.plus(dataService.catalogId))).addProperty(
        DCAT.service, this.createResource(URIref.encode(dataServiceUri.plus(dataService.id)))
    )
}

fun Model.addDataService(dataService: DataService, dataServiceUri: String) {
    val dataServiceResource = this.createResource(URIref.encode(dataServiceUri.plus(dataService.id))).addProperty(
        RDF.type, DCAT.DataService
    )

    dataService.endpointUrl.let {
        dataServiceResource.addProperty(
            DCAT.endpointURL, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.title.let { title ->
        listOf(
            "nb" to title.nb,
            "nn" to title.nn,
            "en" to title.en,
        ).forEach { (lang, value) ->
            value?.let {
                dataServiceResource.addProperty(
                    DCTerms.title, ResourceFactory.createLangLiteral(it, lang)
                )
            }
        }
    }

    dataService.keywords?.let { keyword ->
        listOf(
            "nb" to keyword.nb,
            "nn" to keyword.nn,
            "en" to keyword.en,
        ).forEach { (lang, value) ->
            value?.forEach {
                dataServiceResource.addProperty(
                    DCAT.keyword, ResourceFactory.createLangLiteral(it, lang)
                )
            }
        }
    }

    dataService.endpointDescriptions?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.endpointDescription, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.formats?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCTerms.format, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.contactPoint?.let { point ->
        val contactPointResource = this.createResource().addProperty(
            RDF.type, VCARD4.Organization
        ).addProperty(
            VCARD4.fn, "Contact information | (${dataService.catalogId})"
        )

        point.organizationUnit?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasOrganizationName, ResourceFactory.createLangLiteral(it, "nb")
            )
        }

        point.phone?.takeIf(String::isNotBlank)?.let {
            val telephoneTypeResource = this.createResource().addProperty(
                RDF.type, VCARD4.TelephoneType
            ).addProperty(
                VCARD4.hasValue, ResourceFactory.createResource(URIref.encode("tel:$it"))
            )

            contactPointResource.addProperty(
                VCARD4.hasTelephone, telephoneTypeResource
            )
        }

        point.email?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasEmail, ResourceFactory.createResource(URIref.encode("mailto:$it"))
            )
        }

        point.url?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasURL, ResourceFactory.createResource(URIref.encode(it))
            )
        }

        dataServiceResource.addProperty(
            DCAT.contactPoint, contactPointResource
        )
    }

    dataService.themes?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.theme, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.servesDataset?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.servesDataset, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.description?.let { description ->
        listOf(
            "nb" to description.nb,
            "nn" to description.nn,
            "en" to description.en,
        ).forEach { (lang, value) ->
            value?.let {
                dataServiceResource.addProperty(
                    DCTerms.description, ResourceFactory.createLangLiteral(it, lang)
                )
            }
        }
    }

    dataService.pages?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            FOAF.page, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.landingPage?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCAT.landingPage, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.license?.url?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.license, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.mediaTypes?.filter(String::isNotBlank)?.forEach { type ->
        if (type.startsWith("https://www.iana.org/assignments/media-types/")) {
            dataServiceResource.addProperty(
                DCAT.mediaType, ResourceFactory.createResource(URIref.encode(type))
            )
        } else {
            logger.warn("Non iana media type {} on data service {} was skipped", type, dataService.id)
        }
    }

    dataService.accessRights?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.accessRights, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.type?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.type, ResourceFactory.createResource(URIref.encode(it))
        )
    }
}

fun Model.serialize(lang: Lang): String {
    val stringWriter = StringWriter()
    this.write(stringWriter, lang.name)

    return stringWriter.buffer.toString()
}

private val logger: Logger = LoggerFactory.getLogger(RDFHandler::class.java)
