package no.fdk.catalog.dataservice.core.handler

import no.fdk.catalog.dataservice.core.domain.DataServiceCount
import no.fdk.catalog.dataservice.core.repository.DataServiceRepository
import org.springframework.stereotype.Component

@Component
class CountHandler(private val repository: DataServiceRepository) {

    fun findAll(): List<DataServiceCount> {
        return repository.findAll()
            .groupBy { it.catalogId }
            .map { (catalogId, dataServices) ->
                DataServiceCount(
                    catalogId = catalogId,
                    dataServiceCount = dataServices.distinctBy { it.id }.count()
                )
            }
    }

    fun findSelected(catalogIds: Set<String>): List<DataServiceCount> {
        return repository.findAllByCatalogIdIn(catalogIds)
            .groupBy { it.catalogId }
            .map { (catalogId, dataServices) ->
                DataServiceCount(
                    catalogId = catalogId,
                    dataServiceCount = dataServices.distinctBy { it.id }.count()
                )
            }
    }
}
