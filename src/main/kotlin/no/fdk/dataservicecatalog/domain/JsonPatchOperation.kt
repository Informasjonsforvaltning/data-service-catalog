package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonValue

data class JsonPatchOperation(
    val op: OpEnum,
    val path: String,
    val value: Any? = null,
    val from: String? = null
)

enum class OpEnum(val value: String) {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy");

    @JsonValue
    fun jsonValue(): String = value
}
