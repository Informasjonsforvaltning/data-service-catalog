package no.fdk.dataservicecatalog.integration.config

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Test security configuration for @WebMvcTest tests.
 * This replaces the real SecurityConfig to avoid OAuth2 JWT configuration issues.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@ImportAutoConfiguration(SecurityAutoConfiguration::class, OAuth2ResourceServerAutoConfiguration::class)
class WebMvcTestSecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.disable() }
            .csrf { it.disable() }
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
