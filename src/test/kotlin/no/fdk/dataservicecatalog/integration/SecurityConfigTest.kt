package no.fdk.dataservicecatalog.integration

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@ActiveProfiles("test")
class SecurityConfigTest(@Autowired val mockMvc: MockMvc) {

    @ParameterizedTest
    @ValueSource(strings = ["/ping", "/ready"])
    fun `should not require authentication on ping and ready`(endpoint: String) {
        mockMvc.get(endpoint).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should respond with ok on jwt authentication`() {
        mockMvc.get("/catalogs") {
            with(jwt())
        }.andExpect {
            status { isNotImplemented() }
        }
    }

    @Test
    fun `should respond with unauthorized on missing jwt authentication`() {
        mockMvc.get("/catalogs").andExpect {
            status { isUnauthorized() }
        }
    }
}
