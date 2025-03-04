package no.fdk.dataservicecatalog.exception

class OpenApiParseException(messages: List<String>?) : RuntimeException(
    messages?.joinToString(separator = "\n") { it } ?: "An error occurred"
) {
    val messages: List<String> = messages ?: emptyList()
}
