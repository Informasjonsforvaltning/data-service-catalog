package no.fdk.dataservicecatalog

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(val baseUri: String, val organizationCatalogBaseUri: String)
