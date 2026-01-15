package no.fdk.dataservicecatalog.integration.controller

import no.fdk.dataservicecatalog.controller.CountController
import no.fdk.dataservicecatalog.integration.config.WebMvcTestSecurityConfig
import no.fdk.dataservicecatalog.domain.DataServiceCount
import no.fdk.dataservicecatalog.handler.CountHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@Tag("integration")
@ActiveProfiles("test")

@Import(WebMvcTestSecurityConfig::class)
@WebMvcTest(controllers = [CountController::class])
class CountControllerTest(@param:Autowired val mockMvc: MockMvc) {

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
            with(jwt().jwt { jwt -> jwt.claim("authorities", "system:root:admin") })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].dataServiceCount") { value(5) }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["organization:%s:admin", "organization:%s:write", "organization:%s:read"])
    fun `count should respond with ok and payload on other authority`(authority: String) {
        val catalogId = "123456789"

        handler.stub {
            on { findSelected(setOf(catalogId)) } doReturn listOf(
                DataServiceCount(
                    catalogId = catalogId,
                    dataServiceCount = 5
                )
            )
        }

        mockMvc.get("/internal/catalogs/count") {
            with(jwt().jwt { jwt -> jwt.claim("authorities", authority.format(catalogId)) })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].dataServiceCount") { value(5) }
        }
    }
}
