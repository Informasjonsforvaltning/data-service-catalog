package no.fdk.dataservicecatalog.repository

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.Status
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DataServiceRepository : MongoRepository<DataService, String> {

    fun findAllByCatalogId(catalogId: String): List<DataService>

    fun findAllByCatalogIdIn(catalogIds: Set<String>): List<DataService>

    fun findDataServiceById(dataServiceId: String): DataService?

    fun findAllByStatus(status: Status): List<DataService>

    fun findAllByCatalogIdAndStatus(catalogId: String, status: Status): List<DataService>
}
