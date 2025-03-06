package no.fdk.dataservicecatalog.service

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.LocalizedStrings
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ImportService(private val dataServiceRepository: DataServiceRepository) {

    fun save(dataService: DataService): DataService {
        return dataServiceRepository.save(dataService)
    }

    fun findDataService(internalId: String): DataService? {
        return dataServiceRepository.findByIdOrNull(internalId)
    }

    fun createDataService(externalId: String, catalogId: String): DataService {
        return DataService(
            id = UUID.randomUUID().toString(),
            catalogId = catalogId,
            endpointUrl = externalId,
            title = LocalizedStrings()
        )
    }
}
