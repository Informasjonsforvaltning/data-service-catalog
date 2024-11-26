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
        return repository.existsByCatalogId(catalogId)
            .takeIf { it }
            ?.let { repository.findAllByCatalogIdOrderByCreatedDesc(catalogId) }
            ?: throw NotFoundException("Catalog with id: $catalogId not found")
    }

    fun findById(catalogId: String, dataServiceId: String): DataService {
        if (!repository.existsByCatalogId(catalogId)) {
            throw NotFoundException("Catalog with id: $catalogId not found")
        }

        return repository.findByCatalogIdAndId(catalogId, dataServiceId)
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found")
    }

    fun register(catalogId: String, dataService: DataService): String {
        return UUID.randomUUID().toString().also {
            repository.insert(
                dataService.copy(
                    id = it,
                    catalogId = catalogId,
                    status = dataService.status ?: Status.DRAFT
                )
            )
        }
    }

    fun update(catalogId: String, dataServiceId: String, patchRequest: PatchRequest): DataService {
        if (!repository.existsByCatalogId(catalogId)) {
            throw NotFoundException("Catalog with id: $catalogId not found")
        }

        val dataService = repository.findByCatalogIdAndId(catalogId, dataServiceId)
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found")

        val patchedDataService = patchRequest.patchOperations?.let { operations ->
            patchOriginal(dataService, operations)
        } ?: dataService

        return repository.save(patchedDataService).also {
            logger.info("Updated Data Service with id: $dataServiceId in Catalog with id: $catalogId")
        }
    }

    fun delete(catalogId: String, dataServiceId: String) {
        if (!repository.existsByCatalogId(catalogId)) {
            throw NotFoundException("Catalog with id: $catalogId not found")
        }

        if (!repository.existsById(dataServiceId)) {
            throw NotFoundException("Data Service with id: $dataServiceId not found")
        }

        repository.deleteById(dataServiceId)

        logger.info("Deleted Data Service with id: $dataServiceId in Catalog with id: $catalogId")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DataServiceHandler::class.java)
    }
}
