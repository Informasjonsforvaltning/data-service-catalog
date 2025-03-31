package no.fdk.catalog.dataset

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DatasetApplication

fun main(args: Array<String>) {
    runApplication<DatasetApplication>(*args)
}
