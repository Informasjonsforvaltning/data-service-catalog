package no.fdk.dataservicecatalog.domain

data class DataServiceCount(
    val catalogId: String,
    val serviceCount: Int,
    val publicServiceCount: Int
)
