package no.fdk.dataservicecatalog.handler

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.PatchRequest
import no.fdk.dataservicecatalog.domain.RegisterDataService
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

    fun register(catalogId: String, registerDataService: RegisterDataService): String {
        val id = UUID.randomUUID().toString()

        repository.insert(
            DataService(
                id = id,
                catalogId = catalogId,
                status = registerDataService.status,
                endpointUrl = registerDataService.endpointUrl,
                title = registerDataService.title,
                keywords = registerDataService.keywords,
                endpointDescriptions = registerDataService.endpointDescriptions,
                formats = registerDataService.formats,
                contactPoint = registerDataService.contactPoint,
                themes = registerDataService.themes,
                servesDataset = registerDataService.servesDataset,
                description = registerDataService.description,
                pages = registerDataService.pages,
                landingPage = registerDataService.landingPage,
                license = registerDataService.license,
                mediaTypes = registerDataService.mediaTypes,
                accessRights = registerDataService.accessRights,
                type = registerDataService.type,
                availability = registerDataService.availability,
                versions = registerDataService.versions
            )
        )

        return id
    }

    fun update(catalogId: String, dataServiceId: String, patchRequest: PatchRequest): DataService {
        val dataService = repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")

        val patchedDataService = patchOriginal(dataService, patchRequest.patchOperations)

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
}

private val logger: Logger = LoggerFactory.getLogger(DataServiceHandler::class.java)
