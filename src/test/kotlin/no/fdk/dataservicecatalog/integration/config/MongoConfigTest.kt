package no.fdk.dataservicecatalog.integration.config

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.data.mongodb.core.indexOps
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(MongoDBTestcontainer::class)
class MongoConfigTest(@param:Autowired val operations: MongoOperations) {

    @Test
    fun `should connect to database and create indexes`() {
        val indexOps = operations.indexOps<DataService>()

        assertTrue(hasIndex(indexOps, listOf("catalogId")))
        assertTrue(hasIndex(indexOps, listOf("published")))
        assertTrue(hasIndex(indexOps, listOf("catalogId", "published")))
    }

    private fun hasIndex(indexOps: IndexOperations, name: List<String>) =
        indexOps.indexInfo.any { idx -> idx.isIndexForFields(name) }
}
