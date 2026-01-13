package no.fdk.dataservicecatalog.handler

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.JsonPatchOperation
import no.fdk.dataservicecatalog.domain.DataServiceValues
import no.fdk.dataservicecatalog.entity.DataServiceEntity
import no.fdk.dataservicecatalog.exception.BadRequestException
import no.fdk.dataservicecatalog.exception.NotFoundException
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class DataServiceHandler(private val repository: DataServiceRepository) {

    private fun DataServiceEntity.toDataService(): DataService {
        val values = jacksonObjectMapper().convertValue<DataServiceValues>(data)

        return DataService(
            id = id,
            catalogId = catalogId,
            published = published,
            publishedDate = publishedDate,
            status = values.status,
            endpointUrl = values.endpointUrl,
            title = values.title,
            keywords = values.keywords,
            endpointDescriptions = values.endpointDescriptions,
            formats = values.formats,
            contactPoint = values.contactPoint,
            themes = values.themes,
            servesDataset = values.servesDataset,
            description = values.description,
            pages = values.pages,
            landingPage = values.landingPage,
            license = values.license,
            mediaTypes = values.mediaTypes,
            accessRights = values.accessRights,
            type = values.type,
            availability = values.availability,
            costs = values.costs
        )
    }

    fun findAll(catalogId: String): List<DataService> {
        return repository.findAllByCatalogId(catalogId)
            .map { it.toDataService() }
    }

    fun findById(catalogId: String, dataServiceId: String): DataService {
        return repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?.toDataService()
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")
    }

    fun register(catalogId: String, values: DataServiceValues): String {
        val id = UUID.randomUUID().toString()

        repository.save(
            DataServiceEntity(
                id = id,
                catalogId = catalogId,
                data = jacksonObjectMapper().convertValue<Map<String, Any>>(values)
            )
        )

        return id
    }

    fun update(catalogId: String, dataServiceId: String, operations: List<JsonPatchOperation>): DataService {
        val entity = repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId")

        val patchedValues = patchOriginal(entity.data, operations)

        return repository.save(entity.copy(data = patchedValues))
            .toDataService()
            .also { logger.info("Updated Data Service with id: $dataServiceId in Catalog with id: $catalogId") }
    }

    fun delete(catalogId: String, dataServiceId: String) {
        val dataService = (repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId"))

        repository.delete(dataService)

        logger.info("Deleted Data Service with id: $dataServiceId in Catalog with id: $catalogId")
    }

    fun publish(catalogId: String, dataServiceId: String) {
        val dataService = (repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId"))

        if (dataService.published) throw BadRequestException("Data Service with id: $dataServiceId is already published")

        repository.save(dataService.copy(published = true, publishedDate = LocalDateTime.now()))

        logger.info("Published Data Service with id: $dataServiceId in Catalog with id: $catalogId")
    }

    fun unpublish(catalogId: String, dataServiceId: String) {
        val dataService = (repository.findDataServiceById(dataServiceId)
            ?.takeIf { it.catalogId == catalogId }
            ?: throw NotFoundException("Data Service with id: $dataServiceId not found in Catalog with id: $catalogId"))

        if (!dataService.published) throw BadRequestException("Data Service with id: $dataServiceId not published")

        repository.save(dataService.copy(published = false, publishedDate = null))

        logger.info("Unpublished Data Service with id: $dataServiceId in Catalog with id: $catalogId")
    }
}

private val logger: Logger = LoggerFactory.getLogger(DataServiceHandler::class.java)
