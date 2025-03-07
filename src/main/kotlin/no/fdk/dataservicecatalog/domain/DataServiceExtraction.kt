package no.fdk.dataservicecatalog.domain

data class DataServiceExtraction(
    val dataService: DataService,
    val extractionRecord: ExtractionRecord
)

val DataServiceExtraction.hasError: Boolean
    get() = extractionRecord.extractResult.hasError()
