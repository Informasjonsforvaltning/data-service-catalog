package no.fdk.dataservicecatalog.domain

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.*
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

    val published: Boolean = false,

    val publishedDate: LocalDateTime? = null,

    @CreatedDate
    val created: LocalDateTime? = null,

    @LastModifiedDate
    val modified: LocalDateTime? = null,

    @LastModifiedBy
    val modifiedBy: User? = null,

    @Version
    val version: Int? = null,

    val catalogId: String,

    val status: String?,

    /*
    endepunktsURL (dcat:endpointURL)
     */
    val endpointUrl: String,

    /*
    tittel (dct:title)
     */
    val title: LocalizedStrings,

    /*
    emneord (dcat:keyword)
     */
    val keywords: LocalizedStringLists? = null,

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
    val description: LocalizedStrings? = null,

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
    val license: String? = null,

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
    val type: String? = null,

    /*
    tilgjengelighet (dcatap:availability)
     */
    val availability: String? = null,

    /*
    har gebyr (cv:hasCost)
    */
    val costs: List<Cost>? = null,
)

data class RegisterDataService(

    val status: String? = null,

    @field:NotBlank(message = "Cannot be blank")
    val endpointUrl: String,

    val title: LocalizedStrings,

    val keywords: LocalizedStringLists? = null,

    val endpointDescriptions: List<String>? = null,

    val formats: List<String>? = null,

    val contactPoint: ContactPoint? = null,

    val themes: List<String>? = null,

    val servesDataset: List<String>? = null,

    val description: LocalizedStrings? = null,

    val pages: List<String>? = null,

    val landingPage: String? = null,

    val license: String? = null,

    val mediaTypes: List<String>? = null,

    val accessRights: String? = null,

    val type: String? = null,

    val availability: String? = null,

    val costs: List<Cost>? = null,
)

data class LocalizedStrings(
    val nb: String? = null,
    val nn: String? = null,
    val en: String? = null,
)

data class LocalizedStringLists(
    val nb: List<String>? = null,
    val nn: List<String>? = null,
    val en: List<String>? = null,
)

data class ContactPoint(
    val name: LocalizedStrings? = null,
    val phone: String? = null,
    val email: String? = null,
    val url: String? = null
)

data class Cost(
    val value: Double? = null,
    val description: LocalizedStrings? = null,
    val documentation: List<String>? = null,
    val currency: String? = null
)
