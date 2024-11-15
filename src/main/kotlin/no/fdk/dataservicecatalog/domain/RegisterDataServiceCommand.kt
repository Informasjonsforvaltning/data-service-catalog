package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegisterDataServiceCommand(

    @field:NotBlank(message = "Cannot be null or blank")
    val endpointUrl: String = "",

    @field:NotEmpty(message = "Cannot be null or empty")
    val title: Map<String, String> = emptyMap(),
)
