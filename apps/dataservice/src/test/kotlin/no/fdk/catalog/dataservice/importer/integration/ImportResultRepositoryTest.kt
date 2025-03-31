package no.fdk.catalog.dataservice.importer.integration

import no.fdk.catalog.common.testsupport.MongoDBTestcontainer
import no.fdk.catalog.dataservice.importer.domain.ExtractResult
import no.fdk.catalog.dataservice.importer.domain.ExtractionRecord
import no.fdk.catalog.dataservice.importer.domain.ImportResult
import no.fdk.catalog.dataservice.importer.domain.ImportResultStatus
import no.fdk.catalog.dataservice.importer.repository.ImportResultRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")

@DataMongoTest
@Import(MongoDBTestcontainer::class)
class ImportResultRepositoryTest(
    @Autowired val operations: MongoOperations,
    @Autowired val repository: ImportResultRepository
) {

    @AfterEach
    fun cleanup() {
        operations.remove(Query(), ImportResult::class.java)
    }

    @Test
    fun `find all by catalog id`() {
        val catalogId = "1234"

        val importResult = ImportResult(
            id = "5678",
            catalogId = catalogId,
            status = ImportResultStatus.COMPLETED
        )

        operations.insert(importResult)

        val importResults = repository.findAllByCatalogId(catalogId)

        assertEquals(1, importResults.count())
    }

    @Test
    fun `find first by status and catalog id and external id`() {
        val catalogId = "1234"
        val externalId = "5678"

        val importResult = ImportResult(
            id = "id",
            catalogId = catalogId,
            status = ImportResultStatus.COMPLETED,
            extractionRecords = listOf(
                ExtractionRecord(
                    internalId = "id",
                    externalId = externalId,
                    extractResult = ExtractResult()
                )
            )
        )

        operations.insert(importResult)

        val importResults = repository.findFirstByStatusAndCatalogIdAndExtractionRecordsExternalId(
            ImportResultStatus.COMPLETED,
            catalogId,
            externalId
        )

        assertNotNull(importResults)
    }
}
