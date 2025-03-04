package no.fdk.dataservicecatalog.repository

import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.domain.ImportResultStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ImportResultRepository : MongoRepository<ImportResult, String> {
    fun findFirstByStatusAndExtractionRecordsExternalId(
        importResultStatus: ImportResultStatus,
        externalId: String
    ): ImportResult?

    fun findAllByCatalogId(catalogId: String): List<ImportResult>
}
