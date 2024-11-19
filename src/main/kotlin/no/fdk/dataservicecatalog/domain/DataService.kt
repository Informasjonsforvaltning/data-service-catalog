package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

/*
https://data.norge.no/specification/dcat-ap-no#Datatjeneste
 */

data class DataService(

    /*
    endepunktsURL (dcat:endpointURL)
     */
    @field:NotBlank(message = "Cannot be null or blank")
    val endpointUrl: String? = null,

    /*
    identifikator (dct:identifier)
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val identifier: String? = null,

    /*
    tittel (dct:title)
     */
    @field:Valid
    @field:NotEmpty(message = "Cannot be null or empty")
    val titles: List<LanguageString>? = null,

    /*
    emneord (dcat:keyword)
     */
    @field:Valid
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
    @field:Valid
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

data class LanguageString(

    @field:NotBlank(message = "Cannot be null or blank")
    val language: String = "",

    @field:NotBlank(message = "Cannot be null or blank")
    val value: String = ""
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
