package no.fdk.dataservicecatalog.domain

data class ExtractionRecord(val internalId: String? = null, val externalId: String, val extractResult: ExtractResult)
