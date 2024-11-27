package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.PatchRequest
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class DataServiceHandler(private val repository: DataServiceRepository) {

    fun findAll(catalogId: String): List<DataService> {
        return repository.findAllByCatalogId(catalogId)
    }

    fun findById(catalogId: String, dataServiceId: String): DataService {
        return repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")
    }

    fun register(catalogId: String, dataService: DataService): String {
        val id = UUID.randomUUID().toString()

        repository.insert(
            dataService.copy(
                id = id,
                catalogId = catalogId,
                status = dataService.status ?: Status.DRAFT
            )
        )

        return id
    }

    fun update(catalogId: String, dataServiceId: String, patchRequest: PatchRequest): DataService {
        val dataService = repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")

        val patchedDataService = patchRequest.patchOperations?.let { operations ->
            patchOriginal(dataService, operations)
        } ?: dataService

        return repository.save(patchedDataService).also {
            logger.info("Updated Data Service with id: $dataServiceId in Catalog with id: $catalogId")
        }
    }

    fun delete(catalogId: String, dataServiceId: String) {
        val dataService = (repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId"))

        repository.delete(dataService)

        logger.info("Deleted Data Service with id: $dataServiceId in Catalog with id: $catalogId")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DataServiceHandler::class.java)
    }
}
