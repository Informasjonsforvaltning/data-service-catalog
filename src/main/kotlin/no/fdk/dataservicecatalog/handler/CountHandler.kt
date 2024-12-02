package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataServiceCount
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.springframework.stereotype.Component

@Component
class CountHandler(private val repository: DataServiceRepository) {

    fun findAll(): List<DataServiceCount> {
        return repository.findAll()
            .groupBy { it.catalogId }
            .mapNotNull { (catalogId, items) ->
                catalogId?.let {
                    DataServiceCount(
                        catalogId = it,
                        dataServiceCount = items.distinctBy { item -> item.id }.count()
                    )
                }
            }
    }

    fun findSelected(catalogIds: Set<String>): List<DataServiceCount> {
        return repository.findAllByCatalogIdIn(catalogIds)
            .groupBy { it.catalogId }
            .mapNotNull { (catalogId, items) ->
                catalogId?.let {
                    DataServiceCount(
                        catalogId = it,
                        dataServiceCount = items.distinctBy { item -> item.id }.count()
                    )
                }
            }
    }
}
