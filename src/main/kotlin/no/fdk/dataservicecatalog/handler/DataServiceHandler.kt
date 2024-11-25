package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.PatchRequest
import no.fdk.dataservicecatalog.exception.CatalogNotFoundException
import no.fdk.dataservicecatalog.exception.DataServiceNotFoundException
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DataServiceHandler(private val repository: DataServiceRepository) {

    fun findAll(catalogId: String): List<DataService> {
        if (!repository.existsByCatalogId(catalogId)) throw CatalogNotFoundException("Catalog with id: $catalogId not found")

        return repository.findAllByCatalogIdOrderByCreatedDesc(catalogId)
    }

    fun findById(catalogId: String, dataServiceId: String): DataService {
        if (!repository.existsByCatalogId(catalogId)) throw CatalogNotFoundException("Catalog with id: $catalogId not found")

        return repository.findByCatalogIdAndId(catalogId, dataServiceId)
            ?: throw DataServiceNotFoundException("Data Service with id: $dataServiceId not found")
    }

    fun register(catalogId: String, dataService: DataService): String {
        return ""
    }

    fun update(catalogId: String, dataServiceId: String, patchRequest: PatchRequest): DataService {
        return DataService()
    }

    fun delete(catalogId: String, dataServiceId: String) {
        if (!repository.existsByCatalogId(catalogId)) throw CatalogNotFoundException("Catalog with id: $catalogId not found")

        if (!repository.existsById(dataServiceId)) throw DataServiceNotFoundException("Data Service with id: $dataServiceId not found")

        repository.deleteById(dataServiceId)

        logger.info("Deleted Data Service with id: $dataServiceId from Catalog with id: $catalogId")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DataServiceHandler::class.java)
    }
}
