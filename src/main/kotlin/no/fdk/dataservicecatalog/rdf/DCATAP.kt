package no.fdk.dataservicecatalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class DCATAP {
    companion object {
        const val NS = "http://data.europa.eu/r5r/"

        val availability: Property = ResourceFactory.createProperty("${NS}availability")
    }
}
