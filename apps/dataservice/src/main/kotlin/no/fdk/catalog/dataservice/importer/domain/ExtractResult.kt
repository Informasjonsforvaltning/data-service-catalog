package no.fdk.catalog.dataservice.importer.domain

import no.fdk.catalog.common.jsonpatch.JsonPatchOperation

data class ExtractResult(
    val operations: List<JsonPatchOperation> = emptyList(),
    val issues: List<Issue> = emptyList()
) {

    fun hasError(): Boolean {
        return issues.any { it.type == IssueType.ERROR }
    }
}

val ExtractionRecord.allOperations: List<JsonPatchOperation>
    get() = this.extractResult.operations.toList()
