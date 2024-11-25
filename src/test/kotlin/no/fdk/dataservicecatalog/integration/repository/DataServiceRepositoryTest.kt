package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(MongoDBTestcontainer::class)
class DataServiceRepositoryTest(
    @Autowired val operations: MongoOperations,
    @Autowired val repository: DataServiceRepository
) {

    @Test
    fun `find all by catalog id should return list of one`() {
        operations.insertAll(
            listOf(
                DataService(catalogId = "1234"),
                DataService(catalogId = "5678")
            )
        )

        assertEquals(1, repository.findAllByCatalogIdOrderByCreatedDesc("1234").size)
    }

    @Test
    fun `find by catalog id and data service id should return data service`() {
        val dataService = operations.insert(DataService(catalogId = "1234"))

        assertNotNull(repository.findByCatalogIdAndId("1234", dataService.id!!))
    }

    @Test
    fun `exists by catalog id should return true`() {
        operations.insert(DataService(catalogId = "1234"))

        assertTrue(repository.existsByCatalogId("1234"))
    }
}
