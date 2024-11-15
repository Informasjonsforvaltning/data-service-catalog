package no.fdk.dataservicecatalog.domain

data class DataService(
    val endpointUrl: String,
    val title: Map<String, String>,
)
