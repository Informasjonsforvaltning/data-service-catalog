package no.fdk.dataservicecatalog.domain

/*
https://data.norge.no/specification/dcat-ap-no#Datatjeneste
 */

data class FindDataServiceQuery(

    /*
    endepunktsURL (dcat:endpointURL)
     */
    val endpointUrl: String,

    /*
    identifikator (dct:identifier)
     */
    val identifier: String,

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
) {
    data class LanguageString(
        val language: String,
        val value: String
    )

    data class ContactPoint(
        val name: String?,
        val phone: String?,
        val email: String?,
        val url: String?
    )

    data class License(
        val name: String?,
        val url: String?
    )
}
