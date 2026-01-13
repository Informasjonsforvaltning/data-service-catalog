package no.fdk.dataservicecatalog.repository

import no.fdk.dataservicecatalog.entity.DataServiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DataServiceRepository : JpaRepository<DataServiceEntity, String> {

    fun findAllByCatalogId(catalogId: String): List<DataServiceEntity>

    fun findAllByCatalogIdIn(catalogIds: Set<String>): List<DataServiceEntity>

    fun findDataServiceById(dataServiceId: String): DataServiceEntity?

    fun findAllByPublished(published: Boolean): List<DataServiceEntity>

    fun findAllByCatalogIdAndPublished(catalogId: String, published: Boolean): List<DataServiceEntity>
}
