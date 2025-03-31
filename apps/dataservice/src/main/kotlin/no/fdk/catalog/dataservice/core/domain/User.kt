package no.fdk.catalog.dataservice.core.domain

data class User(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)
