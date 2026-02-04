package no.fdk.dataservicecatalog.handler

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.dataservicecatalog.ApplicationProperties
import no.fdk.dataservicecatalog.domain.DataServiceValues
import no.fdk.dataservicecatalog.domain.LocalizedStrings
import no.fdk.dataservicecatalog.entity.DataServiceEntity
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.rdf.ADMS
import no.fdk.dataservicecatalog.rdf.CV
import no.fdk.dataservicecatalog.rdf.DCATAP
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
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
import java.lang.Exception

@Component
class RDFHandler(private val repository: DataServiceRepository, private val properties: ApplicationProperties) {

    fun findCatalogs(lang: Lang): String {
        val model = createModel()

        repository.findAllByPublished(true)
            .groupBy(DataServiceEntity::catalogId)
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

        repository.findAllByCatalogIdAndPublished(catalogId, true)
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
                        "foaf" to FOAF.NS,
                        "adms" to ADMS.NS,
                        "dcatap" to DCATAP.NS,
                        "cv" to CV.NS
                    )
                )
            }
    }

    private fun getCatalogUri(): String {
        return buildUri(properties.oldBaseUri, "/catalogs/")
    }

    private fun getDataServiceUri(catalogId: String): String {
        return buildUri(properties.oldBaseUri, "/data-services/")
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
    this.safeCreateResource(baseUri.plus(catalogId)).addProperty(
        RDF.type, DCAT.Catalog
    ).addProperty(
        DCTerms.publisher, safeCreateResource(organizationCatalogUri.plus(catalogId))
    ).addProperty(
        DCTerms.title, ResourceFactory.createLangLiteral("Data service catalog ($catalogId)", "en")
    )

    this.safeCreateResource(organizationCatalogUri.plus(catalogId)).addProperty(
        RDF.type, FOAF.Agent
    ).addProperty(
        DCTerms.identifier, catalogId
    ).addProperty(
        OWL.sameAs, URIref.encode(publisherUri.plus(catalogId))
    )
}

fun Model.addDataServiceToCatalog(dataService: DataServiceEntity, catalogUri: String, dataServiceUri: String) {
    this.getProperty(URIref.encode(catalogUri.plus(dataService.catalogId))).addProperty(
        DCAT.service, this.safeCreateResource(dataServiceUri.plus(dataService.id))
    )
}

fun Model.addDataService(dataService: DataServiceEntity, dataServiceUri: String) {
    val dataServiceResource = this.safeCreateResource(dataServiceUri.plus(dataService.id)).addProperty(
        RDF.type, DCAT.DataService
    )
    val values = jacksonObjectMapper().convertValue<DataServiceValues>(dataService.data)

    values.endpointUrl.let {
        dataServiceResource.addProperty(
            DCAT.endpointURL, safeCreateResource(it)
        )
    }

    values.title.let { title ->
        dataServiceResource.addLangLiteralFromLocalizedStrings(title, DCTerms.title)
    }

    values.keywords?.let { keyword ->
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

    values.endpointDescriptions?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.endpointDescription, safeCreateResource(it)
        )
    }

    values.formats?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCTerms.format, safeCreateResource(it)
        )
    }

    values.contactPoint?.let { point ->
        val contactPointResource = this.safeCreateResource(null).addProperty(
            RDF.type, VCARD4.Organization
        )

        point.name?.let { name ->
            contactPointResource.addLangLiteralFromLocalizedStrings(name, VCARD4.fn)
        }

        point.phone?.takeIf(String::isNotBlank)?.let {
            val telephoneTypeResource = this.safeCreateResource(null).addProperty(
                RDF.type, VCARD4.TelephoneType
            ).addProperty(
                VCARD4.hasValue, telephoneResource(it)
            )

            contactPointResource.addProperty(
                VCARD4.hasTelephone, telephoneTypeResource
            )
        }

        point.email?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasEmail, safeCreateResource("mailto:$it")
            )
        }

        point.url?.takeIf(String::isNotBlank)?.let {
            contactPointResource.addProperty(
                VCARD4.hasURL, safeCreateResource(it)
            )
        }

        dataServiceResource.addProperty(
            DCAT.contactPoint, contactPointResource
        )
    }

    values.status?.takeIf(String::isNotBlank)?.let {
        dataServiceResource.addProperty(
            ADMS.status, safeCreateResource(it)
        )
    }

    values.availability?.takeIf(String::isNotBlank)?.let {
        dataServiceResource.addProperty(
            DCATAP.availability, safeCreateResource(it)
        )
    }

    values.themes?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.theme, safeCreateResource(it)
        )
    }

    values.servesDataset?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            DCAT.servesDataset, safeCreateResource(it)
        )
    }

    values.description?.let { description ->
        dataServiceResource.addLangLiteralFromLocalizedStrings(description, DCTerms.description)
    }

    values.pages?.filter(FileUtils::isURI)?.forEach {
        dataServiceResource.addProperty(
            FOAF.page, safeCreateResource(it)
        )
    }

    values.landingPage?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCAT.landingPage, safeCreateResource(it)
        )
    }

    values.license?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.license, safeCreateResource(it)
        )
    }

    values.mediaTypes?.filter(String::isNotBlank)?.forEach { type ->
        if (type.startsWith("https://www.iana.org/assignments/media-types/")) {
            dataServiceResource.addProperty(
                DCAT.mediaType, safeCreateResource(type)
            )
        } else {
            logger.warn("Non iana media type {} on data service {} was skipped", type, dataService.id)
        }
    }

    values.accessRights?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.accessRights, safeCreateResource(it)
        )
    }

    values.type?.takeIf(FileUtils::isURI)?.let {
        dataServiceResource.addProperty(
            DCTerms.type, safeCreateResource(it)
        )
    }

    values.costs?.forEach { cost ->
        val costResource = this.safeCreateResource(null)
            .addProperty(RDF.type, CV.Cost)

        cost.value?.let {
            costResource.addProperty(
                CV.hasValue, ResourceFactory.createTypedLiteral(it)
            )
        }

        cost.description?.let { description ->
            costResource.addLangLiteralFromLocalizedStrings(description, DCTerms.description)
        }

        cost.documentation?.filter(FileUtils::isURI)?.forEach {
            costResource.addProperty(
                FOAF.page, safeCreateResource(it)
            )
        }

        cost.currency?.takeIf(FileUtils::isURI)?.let {
            costResource.addProperty(
                CV.currency, safeCreateResource(it)
            )
        }

        dataServiceResource.addProperty(CV.hasCost, costResource)
    }

    values.version?.takeIf(String::isNotBlank)?.let {
        dataServiceResource.addProperty(
            ResourceFactory.createProperty("${DCAT.NS}version"), it
        )
    }
}

fun Model.serialize(lang: Lang): String {
    val stringWriter = StringWriter()
    this.write(stringWriter, lang.name)

    return stringWriter.buffer.toString()
}

private fun Model.safeCreateResource(value: String?): Resource =
    try {
        value
            ?.let { URIref.encode(it) }
            ?.let { createResource(value) }
            ?: createResource()
    } catch (e: Exception) {
        createResource()
    }

private fun safeCreateResource(value: String?): Resource =
    try {
        value
            ?.let { URIref.encode(it) }
            ?.let { ResourceFactory.createResource(value) }
            ?: ResourceFactory.createResource()
    } catch (e: Exception) {
        ResourceFactory.createResource()
    }

private fun telephoneResource(telephone: String): Resource =
    telephone.trim { it <= ' ' }
        .filterIndexed { index, c ->
            when {
                index == 0 && c == '+' -> true // global-number-digits
                c in '0'..'9' -> true // digit
                else -> false // skip visual-separator and other content
            }
        }
        .let { safeCreateResource("tel:$it") }

private fun Resource.addLangLiteralFromLocalizedStrings(localizedStrings: LocalizedStrings?, predicate: Property) {
    listOf(
        "nb" to localizedStrings?.nb,
        "nn" to localizedStrings?.nn,
        "en" to localizedStrings?.en,
    ).forEach { (lang, value) ->
        value?.let {
            addProperty(predicate, ResourceFactory.createLangLiteral(it, lang))
        }
    }
}

private val logger: Logger = LoggerFactory.getLogger(RDFHandler::class.java)
