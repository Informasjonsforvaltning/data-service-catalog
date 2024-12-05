package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.LanguageString
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(MongoDBTestcontainer::class)
class DataServiceRepositoryTest(
    @Autowired val operations: MongoOperations,
    @Autowired val repository: DataServiceRepository
) {

    @AfterEach
    fun cleanup() {
        operations.remove(Query(), DataService::class.java)
    }

    @Test
    fun `find all by catalog id`() {
        val firstCatalogId = "123"
        val secondCatalogId = "456"

        val dataService = DataService(
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        operations.insertAll(
            listOf(dataService.copy(catalogId = firstCatalogId), dataService.copy(catalogId = secondCatalogId))
        )

        val dataServices = repository.findAllByCatalogId(firstCatalogId)

        assertEquals(1, dataServices.count())
        assertEquals(firstCatalogId, dataServices.first().catalogId)
    }

    @Test
    fun `find all by catalog ids in`() {
        val firstCatalogId = "123"
        val secondCatalogId = "456"

        val dataService = DataService(
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        operations.insertAll(
            listOf(dataService.copy(catalogId = firstCatalogId), dataService.copy(catalogId = secondCatalogId))
        )

        val dataServices = repository.findAllByCatalogIdIn(setOf(firstCatalogId))

        assertEquals(1, dataServices.count())
        assertEquals(firstCatalogId, dataServices.first().catalogId)
    }

    @Test
    fun `find by data service id`() {
        val dataServiceId = "1234"

        operations.insert(
            DataService(
                id = dataServiceId,
                endpointUrl = "endpointUrl",
                titles = listOf(
                    LanguageString("nb", "title")
                )
            )
        )

        val dataService = repository.findDataServiceById(dataServiceId)

        assertEquals(dataServiceId, dataService?.id)
    }

    @Test
    fun `find all by status`() {
        val status = Status.PUBLISHED

        val dataService = DataService(
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        operations.insertAll(
            listOf(dataService.copy(status = Status.PUBLISHED), dataService.copy(status = Status.DRAFT))
        )

        val dataServices = repository.findAllByStatus(status)

        assertEquals(1, dataServices.count())
        assertEquals(Status.PUBLISHED, dataServices.first().status)
    }

    @Test
    fun `find all by catalog id and status`() {
        val catalogId = "1234"
        val status = Status.PUBLISHED

        val dataService = DataService(
            catalogId = catalogId,
            endpointUrl = "endpointUrl",
            titles = listOf(
                LanguageString("nb", "title")
            )
        )

        operations.insertAll(
            listOf(dataService.copy(status = Status.PUBLISHED), dataService.copy(status = Status.DRAFT))
        )

        val dataServices = repository.findAllByCatalogIdAndStatus(catalogId, status)

        assertEquals(1, dataServices.count())
        assertEquals(Status.PUBLISHED, dataServices.first().status)
    }
}
