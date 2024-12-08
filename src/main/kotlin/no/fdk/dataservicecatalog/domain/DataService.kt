package no.fdk.dataservicecatalog.domain

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/*
https://data.norge.no/specification/dcat-ap-no#Datatjeneste
 */

@Document(collection = "dataServices")
data class DataService(

    /*
    identifikator (dct:identifier)
     */
    @Id
    val id: String,

    @CreatedDate
    val created: LocalDateTime? = null,

    @LastModifiedDate
    val modified: LocalDateTime? = null,

    @Version
    val version: Int? = null,

    val catalogId: String,

    val status: Status,

    /*
    endepunktsURL (dcat:endpointURL)
     */
    val endpointUrl: String,

    /*
    tittel (dct:title)
     */
    val titles: List<LanguageString>,

    /*
    emneord (dcat:keyword)
     */
    val keywords: List<LanguageString>? = null,

    /*
    endepunktsbeskrivelse (dcat:endpointDescription)
     */
    val endpointDescriptions: List<String>? = null,

    /*
    format (dct:format)
     */
    val formats: List<String>? = null,

    /*
    kontaktpunkt (dcat:contactPoint)
     */
    val contactPoint: ContactPoint? = null,

    /*
    tema (dcat:theme)
     */
    val themes: List<String>? = null,

    /*
    tilgjengeliggjør datasett (dcat:servesDataset)
     */
    val servesDataset: List<String>? = null,

    /*
    beskrivelse (dct:description)
     */
    val description: LanguageString? = null,

    /*
    dokumentasjon (foaf:page)
     */
    val pages: List<String>? = null,

    /*
    landingsside (dcat:landingPage)
     */
    val landingPage: String? = null,

    /*
    lisens (dct:license)
     */
    val license: License? = null,

    /*
    medietype (dcat:mediaType)
    */
    val mediaTypes: List<String>? = null,

    /*
    tilgangsrettigheter (dct:accessRights)
     */
    val accessRights: String? = null,

    /*
    type (dct:type)
     */
    val type: String? = null
)

data class RegisterDataService(

    val status: Status? = null,

    @field:NotBlank(message = "Cannot be blank")
    val endpointUrl: String,

    @field:Valid
    @field:NotEmpty(message = "Cannot be empty")
    val titles: List<LanguageString>,

    @field:Valid
    val keywords: List<LanguageString>? = null,

    val endpointDescriptions: List<String>? = null,

    val formats: List<String>? = null,

    val contactPoint: ContactPoint? = null,

    val themes: List<String>? = null,

    val servesDataset: List<String>? = null,

    @field:Valid
    val description: LanguageString? = null,

    val pages: List<String>? = null,

    val landingPage: String? = null,

    val license: License? = null,

    val mediaTypes: List<String>? = null,

    val accessRights: String? = null,

    val type: String? = null
)

data class LanguageString(

    @field:NotBlank(message = "Cannot be blank")
    val language: String,

    @field:NotBlank(message = "Cannot be blank")
    val value: String
)

data class ContactPoint(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val url: String? = null
)

data class License(
    val name: String? = null,
    val url: String? = null
)

enum class Status {
    DRAFT, PUBLISHED
}
