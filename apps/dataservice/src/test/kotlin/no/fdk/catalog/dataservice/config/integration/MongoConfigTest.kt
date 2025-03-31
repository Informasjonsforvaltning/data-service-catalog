package no.fdk.catalog.dataservice.config.integration

import no.fdk.catalog.dataservice.config.MongoConfig
import no.fdk.catalog.dataservice.core.domain.DataService
import no.fdk.catalog.common.testsupport.MongoDBTestcontainer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(value = [MongoDBTestcontainer::class, MongoConfig::class])
class MongoConfigTest(@Autowired val operations: MongoOperations) {

    @Test
    fun `should connect to database and create indexes`() {
        val indexOps = operations.indexOps(DataService::class.java)

        assertTrue(hasIndex(indexOps, listOf("catalogId")))
        assertTrue(hasIndex(indexOps, listOf("published")))
        assertTrue(hasIndex(indexOps, listOf("catalogId", "published")))
    }

    private fun hasIndex(indexOps: IndexOperations, name: List<String>) =
        indexOps.indexInfo.any { idx -> idx.isIndexForFields(name) }
}
