package no.fdk.dataservicecatalog.repository

import no.fdk.dataservicecatalog.domain.DataService
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DataServiceRepository : MongoRepository<DataService, String> {

    fun findAllByCatalogId(catalogId: String): List<DataService>

    fun findDataServiceById(dataServiceId: String): DataService?
}
