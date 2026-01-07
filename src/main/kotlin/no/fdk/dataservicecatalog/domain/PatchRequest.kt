package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.validation.constraints.NotBlank

data class JsonPatchOperation(

    val op: OpEnum,

    @field:NotBlank(message = "Cannot be blank")
    val path: String,

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
