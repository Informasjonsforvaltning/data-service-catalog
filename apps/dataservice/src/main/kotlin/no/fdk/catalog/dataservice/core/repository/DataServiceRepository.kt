package no.fdk.catalog.dataservice.core.repository

import no.fdk.catalog.dataservice.core.domain.DataService
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DataServiceRepository : MongoRepository<DataService, String> {

    fun findAllByCatalogId(catalogId: String): List<DataService>

    fun findAllByCatalogIdIn(catalogIds: Set<String>): List<DataService>

    fun findDataServiceById(dataServiceId: String): DataService?

    fun findAllByPublished(published: Boolean): List<DataService>

    fun findAllByCatalogIdAndPublished(catalogId: String, published: Boolean): List<DataService>
}
