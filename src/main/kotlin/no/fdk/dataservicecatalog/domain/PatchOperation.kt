package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class JsonPatchOperation(

    @field:NotNull(message = "Cannot be null or invalid operator")
    val op: OpEnum? = null,

    @field:NotBlank(message = "Cannot be null or blank")
    val path: String? = null,

    val value: Any? = null,
    val from: String? = null
)

enum class OpEnum(private val value: String) {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy");

    @JsonValue
    fun jsonValue(): String = value
}
