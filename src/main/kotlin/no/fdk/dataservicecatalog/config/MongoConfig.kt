package no.fdk.dataservicecatalog.config

import com.mongodb.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
@EnableMongoAuditing
class MongoConfig {

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, "dataServiceCatalog")
    }
}
