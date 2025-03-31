package no.fdk.catalog.dataservice.importer.domain

import no.fdk.catalog.dataservice.core.domain.DataService

data class DataServiceExtraction(
    val dataService: DataService,
    val extractionRecord: ExtractionRecord
)

val Iterable<DataServiceExtraction>.hasError: Boolean
    get() = any { it.extractionRecord.extractResult.hasError() }

val Iterable<DataServiceExtraction>.allExtractionRecords: List<ExtractionRecord>
    get() = map { it.extractionRecord }
