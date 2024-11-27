package no.fdk.dataservicecatalog.integration.config

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import org.junit.jupiter.api.Assertions.assertTrue
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
class MongoConfigTest(@Autowired val mongoTemplate: MongoOperations) {

    @Test
    fun `should connect to database and create index`() {
        val hasCatalogIdIndex = mongoTemplate.indexOps(DataService::class.java)
            .indexInfo.any { idx -> idx.isIndexForFields(listOf("catalogId")) }

        assertTrue(hasCatalogIdIndex)
    }
}
