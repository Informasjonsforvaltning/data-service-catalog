package no.fdk.dataservicecatalog.domain

data class ExtractionRecord(val internalId: String, val externalId: String, val extractResult: ExtractResult)
