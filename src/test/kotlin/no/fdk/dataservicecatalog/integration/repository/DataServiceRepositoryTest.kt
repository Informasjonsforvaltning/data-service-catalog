package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.Status
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setup() {
        operations.remove(Query(), DataService::class.java)
    }

    @Test
    fun `find all by catalog id`() {
        val catalogId = "1234"

        operations.insertAll(
            listOf(
                DataService(catalogId = catalogId),
                DataService(catalogId = "5678")
            )
        )

        assertEquals(1, repository.findAllByCatalogId(catalogId).size)
    }

    @Test
    fun `find by data service id`() {
        val dataServiceId = "1234"

        operations.insert(DataService(id = dataServiceId))

        assertEquals(dataServiceId, repository.findDataServiceById(dataServiceId)?.id)
    }

    @Test
    fun `find all by status`() {
        val status = Status.PUBLISHED

        operations.insertAll(
            listOf(
                DataService(status = Status.PUBLISHED),
                DataService(status = Status.DRAFT)
            )
        )

        assertEquals(1, repository.findAllByStatus(status).size)
    }

    @Test
    fun `find all by catalog id and status`() {
        val catalogId = "1234"
        val status = Status.PUBLISHED

        operations.insertAll(
            listOf(
                DataService(catalogId = catalogId, status = Status.PUBLISHED),
                DataService(catalogId = catalogId, status = Status.DRAFT)
            )
        )

        assertEquals(1, repository.findAllByCatalogIdAndStatus(catalogId, status).size)
    }
}
