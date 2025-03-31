package no.fdk.catalog.dataservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "no.fdk.catalog.dataservice",
        "no.fdk.catalog.common"
    ]
)
@ConfigurationPropertiesScan
class DataServiceApplication

fun main(args: Array<String>) {
    runApplication<DataServiceApplication>(*args)
}
