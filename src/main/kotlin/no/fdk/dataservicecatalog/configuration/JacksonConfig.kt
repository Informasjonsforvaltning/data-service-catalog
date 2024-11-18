package no.fdk.dataservicecatalog.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.json.ProblemDetailJacksonMixin

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.NullIsSameAsDefault, true)
                    .build()
            )
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .addMixIn(ProblemDetail::class.java, ProblemDetailJacksonMixin::class.java)
    }
}
