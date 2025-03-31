package no.fdk.catalog.dataservice.importer.service

import no.fdk.catalog.dataservice.importer.domain.ExtractionRecord
import no.fdk.catalog.dataservice.importer.domain.ImportResult
import no.fdk.catalog.dataservice.importer.domain.ImportResultStatus
import no.fdk.catalog.dataservice.importer.repository.ImportResultRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ImportResultService(private val importResultRepository: ImportResultRepository) {

    fun getResults(catalogId: String): List<ImportResult> {
        return importResultRepository.findAllByCatalogId(catalogId)
    }

    fun getResult(statusId: String): ImportResult? {
        return importResultRepository.findByIdOrNull(statusId)
    }

    fun deleteResult(result: ImportResult) {
        importResultRepository.delete(result)
    }

    fun findDataServiceIdByCatalogIdAndExternalId(catalogId: String, externalId: String): String? {
        return importResultRepository
            .findFirstByStatusAndCatalogIdAndExtractionRecordsExternalId(
                ImportResultStatus.COMPLETED,
                catalogId,
                externalId
            )
            ?.extractionRecords
            ?.firstOrNull { it.externalId == externalId }
            ?.internalId
    }

    fun save(
        catalogId: String,
        extractionRecords: List<ExtractionRecord>,
        status: ImportResultStatus
    ): ImportResult {
        return importResultRepository.save(
            ImportResult(
                id = UUID.randomUUID().toString(),
                created = LocalDateTime.now(),
                catalogId = catalogId,
                status = status,
                extractionRecords = extractionRecords
            )
        )
    }
}
