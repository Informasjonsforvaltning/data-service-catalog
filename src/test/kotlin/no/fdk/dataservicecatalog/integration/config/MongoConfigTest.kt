package no.fdk.dataservicecatalog.integration.config

import no.fdk.dataservicecatalog.integration.IntegrationTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
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
@Import(IntegrationTestConfig::class)
class MongoConfigTest(@Autowired val mongoTemplate: MongoOperations) {

    @Test
    fun `should connect to database`() {
        val collection = mongoTemplate.createCollection("test")

        assertEquals(0L, collection.countDocuments())
    }
}
