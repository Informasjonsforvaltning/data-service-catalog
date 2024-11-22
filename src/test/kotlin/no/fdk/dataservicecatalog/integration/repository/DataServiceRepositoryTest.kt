package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(MongoDBTestcontainer::class)
class DataServiceRepositoryTest(@Autowired val mongoTemplate: MongoOperations) {
}
