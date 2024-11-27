package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.apache.jena.riot.Lang
import org.springframework.stereotype.Component

@Component
class RDFHandler(private val repository: DataServiceRepository) {

    fun findAll(lang: Lang): String {
        return ""
    }

    fun findById(catalogId: String, lang: Lang): String {
        return ""
    }

    fun findById(catalogId: String, dataServiceId: String, lang: Lang): String {
        return ""
    }
}
