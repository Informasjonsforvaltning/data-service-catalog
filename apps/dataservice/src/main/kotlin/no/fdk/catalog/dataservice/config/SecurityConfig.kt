package no.fdk.catalog.dataservice.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableMethodSecurity
class SecurityConfig(@Value("\${application.cors.originPatterns}") private val corsOriginPatterns: Array<String>) {
    private val logger: Logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { cors ->
                cors.configurationSource { _ ->
                    val config = CorsConfiguration()
                    config.allowCredentials = false
                    config.allowedOriginPatterns = corsOriginPatterns.toList()
                    config.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PATCH")
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L

                    logger.debug("CORS configuration allowed origin patterns: {}", config.allowedOriginPatterns)

                    config
                }
            }
            .csrf {
                it.disable()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.GET, "/ping", "/ready", "/catalogs/**", "/swagger-ui/**", "/v3/**")
                    .permitAll()
                authorize.anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer -> resourceServer.jwt { } }
            .build()
    }
}
