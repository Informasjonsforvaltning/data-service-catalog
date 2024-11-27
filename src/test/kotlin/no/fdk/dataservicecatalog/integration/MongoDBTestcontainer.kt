package no.fdk.dataservicecatalog.integration

import no.fdk.dataservicecatalog.config.MongoConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.testcontainers.containers.MongoDBContainer

@Import(MongoConfig::class)
@TestConfiguration(proxyBeanMethods = false)
class MongoDBTestcontainer {

    @Bean
    @ServiceConnection
    fun mongoDBContainer(): MongoDBContainer {
        return MongoDBContainer("mongo:latest")
    }
}
