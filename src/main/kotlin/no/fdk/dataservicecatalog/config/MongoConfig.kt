package no.fdk.dataservicecatalog.config

import com.mongodb.client.MongoClient
import no.fdk.dataservicecatalog.domain.DataService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

@Configuration
@EnableMongoAuditing
class MongoConfig {

    @Value("\${spring.data.mongodb.database}")
    private lateinit var database: String

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoOperations {
        return MongoTemplate(mongoClient, database)
    }

    @Bean
    fun configureIndexes(mongoOperations: MongoOperations): Boolean {
        mongoOperations.indexOps(DataService::class.java)
            .ensureIndex(Index().on("catalogId", Sort.Direction.ASC))

        return true
    }
}
