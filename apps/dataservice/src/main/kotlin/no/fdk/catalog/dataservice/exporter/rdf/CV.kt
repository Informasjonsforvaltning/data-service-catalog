package no.fdk.catalog.dataservice.exporter.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class CV {
    companion object {
        const val NS = "http://data.europa.eu/m8g/"

        val Cost: Resource = ResourceFactory.createResource("${NS}Cost")

        val currency: Property = ResourceFactory.createProperty("${NS}currency")
        val hasCost: Property = ResourceFactory.createProperty("${NS}hasCost")
        val hasValue: Property = ResourceFactory.createProperty("${NS}hasValue")
    }
}
