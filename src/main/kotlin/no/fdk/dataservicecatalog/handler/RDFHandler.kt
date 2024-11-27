package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.util.URIref
import org.apache.jena.vocabulary.*
import org.springframework.stereotype.Component
import java.io.StringWriter

@Component
class RDFHandler(private val repository: DataServiceRepository) {

    fun findAll(lang: Lang): String {
        val dataServices = repository.findAllByStatus(Status.PUBLISHED)

        if (dataServices.isEmpty()) {
            return ""
        }

        val model = createModel()

        val dataServicesByCatalogId = dataServices.groupBy(DataService::catalogId)

        for (entry in dataServicesByCatalogId) {
            model.addCatalog(entry.key!!)
        }

        return model.serialise(lang)
    }

    fun findById(catalogId: String, lang: Lang): String {
        return ""
    }

    fun findById(catalogId: String, dataServiceId: String, lang: Lang): String {
        return ""
    }

    private fun createModel(): Model {
        return ModelFactory
            .createDefaultModel()
            .setNsPrefixes(
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

fun Model.addCatalog(catalogId: String) {
    this.createResource(URIref.encode("/catalogs/".plus(catalogId)))
        .addProperty(RDF.type, DCAT.Catalog)
        .addProperty(DCTerms.publisher, ResourceFactory.createResource(URIref.encode(catalogId)))
        .addProperty(DCTerms.title, ResourceFactory.createLangLiteral("Data service catalog ($catalogId)", "en"))

    this.createResource(URIref.encode("/organizations/".plus(catalogId)))
        .addProperty(RDF.type, FOAF.Agent)
        .addProperty(DCTerms.identifier, catalogId)
        .addProperty(OWL.sameAs, URIref.encode(catalogId))
}

fun Model.serialise(lang: Lang): String {
    val stringWriter = StringWriter()
    this.write(stringWriter, lang.name)

    return stringWriter.buffer.toString()
}
