package no.fdk.dataservicecatalog.integration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer

@TestConfiguration(proxyBeanMethods = false)
class MongoDBTestcontainer {

    @Bean
    @ServiceConnection
    fun mongoDBContainer(): MongoDBContainer {
        return MongoDBContainer("mongo:latest")
    }
}
