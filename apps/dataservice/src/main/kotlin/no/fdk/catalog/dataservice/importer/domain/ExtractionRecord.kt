package no.fdk.catalog.dataservice.importer.domain

data class ExtractionRecord(val internalId: String, val externalId: String, val extractResult: ExtractResult)
