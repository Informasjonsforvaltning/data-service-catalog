package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.LocalizedStrings
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
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        operations.insertAll(
            listOf(dataService, dataService.copy(id = "2222", catalogId = secondCatalogId))
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
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        operations.insertAll(
            listOf(dataService, dataService.copy(id = "2222", catalogId = secondCatalogId))
        )

        val dataServices = repository.findAllByCatalogIdIn(setOf(firstCatalogId))

        assertEquals(1, dataServices.count())
        assertEquals(firstCatalogId, dataServices.first().catalogId)
    }

    @Test
    fun `find by data service id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        operations.insert(
            DataService(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                status = null,
                endpointUrl = "endpointUrl",
                title = LocalizedStrings(nb = "title")
            )
        )

        val dataService = repository.findDataServiceById(dataServiceId)

        assertEquals(dataServiceId, dataService?.id)
    }

    @Test
    fun `find all by published status`() {
        val catalogId = "1234"

        val dataService = DataService(
            id = "1111",
            catalogId = catalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        operations.insertAll(
            listOf(dataService, dataService.copy(id = "2222", published = false))
        )

        val dataServices = repository.findAllByPublished(true)

        assertEquals(1, dataServices.count())
        assertEquals(true, dataServices.first().published)
    }

    @Test
    fun `find all by catalog id and published status`() {
        val catalogId = "1234"

        val dataService = DataService(
            id = "1111",
            catalogId = catalogId,
            published = true,
            status = null,
            endpointUrl = "endpointUrl",
            title = LocalizedStrings(nb = "title")
        )

        operations.insertAll(
            listOf(dataService, dataService.copy(id = "2222", published = false))
        )

        val dataServices = repository.findAllByCatalogIdAndPublished(catalogId, true)

        assertEquals(1, dataServices.count())
        assertEquals(true, dataServices.first().published)
    }
}
