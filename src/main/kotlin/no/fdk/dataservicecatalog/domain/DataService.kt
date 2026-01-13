package no.fdk.dataservicecatalog.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

data class DataService(

    val id: String,
    val published: Boolean = false,
    val publishedDate: LocalDateTime? = null,
    val catalogId: String,

    override val status: String?,
    override val endpointUrl: String?,
    override val title: LocalizedStrings?,
    override val keywords: LocalizedStringLists?,
    override val endpointDescriptions: List<String>?,
    override val formats: List<String>?,
    override val contactPoint: ContactPoint?,
    override val themes: List<String>?,
    override val servesDataset: List<String>?,
    override val description: LocalizedStrings?,
    override val pages: List<String>?,
    override val landingPage: String?,
    override val license: String?,
    override val mediaTypes: List<String>?,
    override val accessRights: String?,
    override val type: String?,
    override val availability: String?,
    override val costs: List<Cost>?,
) : DataServiceValues(
    status,
    endpointUrl,
    title,
    keywords,
    endpointDescriptions,
    formats,
    contactPoint,
    themes,
    servesDataset,
    description,
    pages,
    landingPage,
    license,
    mediaTypes,
    accessRights,
    type,
    availability,
    costs,
)

/*
https://data.norge.no/specification/dcat-ap-no#Datatjeneste
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
open class DataServiceValues(

    /*
    status (adms:status)
     */
    open val status: String?,

    /*
    endepunktsURL (dcat:endpointURL)
     */
    open val endpointUrl: String?,

    /*
    tittel (dct:title)
     */
    open val title: LocalizedStrings?,

    /*
    emneord (dcat:keyword)
     */
    open val keywords: LocalizedStringLists?,

    /*
    endepunktsbeskrivelse (dcat:endpointDescription)
     */
    open val endpointDescriptions: List<String>?,

    /*
    format (dct:format)
     */
    open val formats: List<String>?,

    /*
    kontaktpunkt (dcat:contactPoint)
     */
    open val contactPoint: ContactPoint?,

    /*
    tema (dcat:theme)
     */
    open val themes: List<String>?,

    /*
    tilgjengeliggjør datasett (dcat:servesDataset)
     */
    open val servesDataset: List<String>?,

    /*
    beskrivelse (dct:description)
     */
    open val description: LocalizedStrings?,

    /*
    dokumentasjon (foaf:page)
     */
    open val pages: List<String>?,

    /*
    landingsside (dcat:landingPage)
     */
    open val landingPage: String?,

    /*
    lisens (dct:license)
     */
    open val license: String?,

    /*
    medietype (dcat:mediaType)
    */
    open val mediaTypes: List<String>?,

    /*
    tilgangsrettigheter (dct:accessRights)
     */
    open val accessRights: String?,

    /*
    type (dct:type)
     */
    open val type: String?,

    /*
    tilgjengelighet (dcatap:availability)
     */
    open val availability: String?,

    /*
    har gebyr (cv:hasCost)
    */
    open val costs: List<Cost>?,
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
