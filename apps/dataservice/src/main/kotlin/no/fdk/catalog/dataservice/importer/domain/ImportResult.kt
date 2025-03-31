package no.fdk.catalog.dataservice.importer.domain

import no.fdk.catalog.dataservice.core.domain.User
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

enum class ImportResultStatus { FAILED, COMPLETED }

@Document(collection = "importResults")
data class ImportResult(

    @Id
    val id: String,

    @CreatedDate
    val created: LocalDateTime? = null,

    @LastModifiedDate
    val modified: LocalDateTime? = null,

    @LastModifiedBy
    val modifiedBy: User? = null,

    @Version
    val version: Int? = null,

    val catalogId: String,
    val status: ImportResultStatus,
    val extractionRecords: List<ExtractionRecord> = emptyList()
)
