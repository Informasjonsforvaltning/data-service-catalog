package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.config.JacksonConfig
import no.fdk.dataservicecatalog.config.SecurityConfig
import no.fdk.dataservicecatalog.controller.CountController
import no.fdk.dataservicecatalog.domain.DataServiceCount
import no.fdk.dataservicecatalog.handler.CountHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Tag("integration")
@ActiveProfiles("test")

@Import(SecurityConfig::class, JacksonConfig::class)
@WebMvcTest(controllers = [CountController::class])
class CountControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var handler: CountHandler

    @Test
    fun `count should respond with ok and payload on admin`() {
        handler.stub {
            on { findAll() } doReturn listOf(
                DataServiceCount(
                    catalogId = "1234",
                    dataServiceCount = 5
                )
            )
        }

        mockMvc.get("/internal/catalogs/count") {
            with(jwt().authorities(SimpleGrantedAuthority("system:root:admin")))
        }.andExpect {
            status { isOk() }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:%s:admin", "organization:%s:write", "organization:%s:read"])
    fun `count should respond with ok and payload on other authority`(authority: String) {
        val catalogId = "1234"

        handler.stub {
            on { findAll() } doReturn listOf(
                DataServiceCount(
                    catalogId = catalogId,
                    dataServiceCount = 5
                )
            )
        }

        mockMvc.get("/internal/catalogs/count") {
            with(jwt().authorities(SimpleGrantedAuthority(authority.format(catalogId))))
        }.andExpect {
            status { isOk() }
        }
    }
}
