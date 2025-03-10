package no.fdk.dataservicecatalog.domain

data class DataServiceExtraction(
    val dataService: DataService,
    val extractionRecord: ExtractionRecord
)

val Iterable<DataServiceExtraction>.hasError: Boolean
    get() = any { it.extractionRecord.extractResult.hasError() }

val Iterable<DataServiceExtraction>.allExtractionRecords: List<ExtractionRecord>
    get() = map { it.extractionRecord }
