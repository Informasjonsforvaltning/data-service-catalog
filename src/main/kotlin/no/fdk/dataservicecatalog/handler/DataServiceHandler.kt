package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.springframework.stereotype.Component

@Component
class DataServiceHandler(private val repository: DataServiceRepository) {

    fun findAll(catalogId: String): List<DataService> {
        return emptyList()
    }

    fun findById(catalogId: String, dataServiceId: String): DataService {
        return DataService(dataServiceId)
    }

    fun register(catalogId: String, dataService: DataService): String {
        return ""
    }
}
