package no.fdk.catalog.dataservice.importer.repository

import no.fdk.catalog.dataservice.importer.domain.ImportResult
import no.fdk.catalog.dataservice.importer.domain.ImportResultStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ImportResultRepository : MongoRepository<ImportResult, String> {

    fun findFirstByStatusAndCatalogIdAndExtractionRecordsExternalId(
        importResultStatus: ImportResultStatus,
        catalogId: String,
        externalId: String
    ): ImportResult?

    fun findAllByCatalogId(catalogId: String): List<ImportResult>
}
