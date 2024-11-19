package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class PatchRequest(

    @JsonValue
    @field:Valid
    @field:NotEmpty(message = "Cannot be null or empty")
    val patchOperations: List<JsonPatchOperation>? = null
)
