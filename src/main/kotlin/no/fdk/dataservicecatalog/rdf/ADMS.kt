package no.fdk.dataservicecatalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class ADMS {
    companion object {
        const val NS = "http://www.w3.org/ns/adms#"

        val status: Property = ResourceFactory.createProperty("${NS}status")
    }
}
