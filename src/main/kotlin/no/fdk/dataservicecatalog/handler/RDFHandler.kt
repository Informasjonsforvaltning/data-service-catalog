package no.fdk.dataservicecatalog.handler

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
import org.springframework.stereotype.Component
import java.io.StringWriter

@Component
class RDFHandler(private val repository: DataServiceRepository) {

    fun findAll(lang: Lang): String {
        val dataServices = repository.findAllByStatus(Status.PUBLISHED).groupBy(DataService::catalogId)

        if (dataServices.isEmpty()) {
            return ""
        }

        val model = createModel()

        for (entry in dataServices) {
            entry.key?.let { model.addCatalog(it) }

            entry.value.forEach { dataService ->
                model.addDataServiceToCatalog(dataService)
                model.addDataService(dataService)
            }
        }

        return model.serialize(lang)
    }

    fun findById(catalogId: String, lang: Lang): String {
        val dataServices = repository.findAllByCatalogIdAndStatus(catalogId, Status.PUBLISHED)

        if (dataServices.isEmpty()) {
            return ""
        }

        val model = createModel()

        dataServices.forEach { dataService ->
            dataService.catalogId?.let { model.addCatalog(it) }
            model.addDataServiceToCatalog(dataService)
            model.addDataService(dataService)
        }

        return model.serialize(lang)
    }

    fun findById(catalogId: String, dataServiceId: String, lang: Lang): String {
        val dataService = repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")

        val model = createModel()
        model.addDataService(dataService)

        return model.serialize(lang)
    }

    private fun createModel(): Model {
        return ModelFactory.createDefaultModel().setNsPrefixes(
            mapOf(
                "dcat" to DCAT.NS, "dct" to DCTerms.NS, "rdf" to RDF.uri, "vcard" to VCARD4.NS, "foaf" to FOAF.NS
            )
        )
    }
}

fun Model.addCatalog(catalogId: String) {
    this.createResource(URIref.encode("/catalogs/".plus(catalogId))).addProperty(RDF.type, DCAT.Catalog)
        .addProperty(
            DCTerms.publisher, ResourceFactory.createResource(URIref.encode(catalogId))
        ).addProperty(
            DCTerms.title, ResourceFactory.createLangLiteral("Data service catalog ($catalogId)", "en")
        )

    this.createResource(URIref.encode("/organizations/".plus(catalogId)))
        .addProperty(
            RDF.type, FOAF.Agent
        ).addProperty(
            DCTerms.identifier, catalogId
        ).addProperty(
            OWL.sameAs, URIref.encode(catalogId)
        )
}

fun Model.addDataServiceToCatalog(dataService: DataService) {
    this.getProperty(URIref.encode("/catalogs/".plus(dataService.catalogId)))
        .addProperty(
            DCAT.service, this.createResource(URIref.encode("/data-services/".plus(dataService.id)))
        )
}

fun Model.addDataService(dataService: DataService) {
    val dataServiceResource = this.createResource(URIref.encode("/data-services/".plus(dataService.id)))
        .addProperty(
            RDF.type, DCAT.DataService
        )

    dataService.endpointUrl?.let {
        dataServiceResource.addProperty(
            DCAT.endpointURL, ResourceFactory.createResource(URIref.encode(it))
        )
    }

    dataService.titles?.forEach {
        dataServiceResource.addProperty(
            DCTerms.title, ResourceFactory.createLangLiteral(it.value, it.language)
        )
    }

    dataService.keywords?.forEach {
        dataServiceResource.addProperty(
            DCAT.keyword, ResourceFactory.createLangLiteral(it.value, it.language)
        )
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

    dataService.contactPoint?.let {
        val contactPointResource = this.createResource()
            .addProperty(
                RDF.type, VCARD4.Organization
            ).addProperty(
                VCARD4.fn, "Contact information | (${dataService.catalogId})"
            )

        it.name?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasOrganizationName, ResourceFactory.createLangLiteral(it, "nb")
            )
        }

        it.phone?.takeIf(String::isNotBlank)?.let {
            val telephoneTypeResource =
                this.createResource()
                    .addProperty(
                        RDF.type, VCARD4.TelephoneType
                    ).addProperty(
                        VCARD4.hasValue, ResourceFactory.createResource(URIref.encode("tel:$it"))
                    )

            contactPointResource.addProperty(
                VCARD4.hasTelephone, telephoneTypeResource
            )
        }

        it.email?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasEmail, ResourceFactory.createResource(URIref.encode("mailto:$it"))
            )
        }

        it.url?.takeIf(String::isNotBlank)?.let {
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

    dataService.description?.let {
        dataServiceResource.addProperty(
            DCTerms.description, ResourceFactory.createLangLiteral(it.value, it.language)
        )
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
        dataServiceResource.addProperty(
            DCAT.mediaType, ResourceFactory.createResource(
                URIref.encode(
                    if (type.startsWith("https://www.iana.org/assignments/media-types/")) type
                    else "https://www.iana.org/assignments/media-types/$type"
                )
            )
        )
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
