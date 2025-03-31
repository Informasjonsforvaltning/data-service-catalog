package no.fdk.catalog.dataservice.config.unit

import no.fdk.catalog.dataservice.config.MongoConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

@Tag("unit")
@ExtendWith(MockitoExtension::class)
class MongoConfigTest {

    @Mock
    lateinit var securityContext: SecurityContext

    @Mock
    lateinit var authentication: Authentication

    @Mock
    lateinit var jwt: Jwt

    private lateinit var config: MongoConfig

    @BeforeEach
    fun setup() {
        SecurityContextHolder.setContext(securityContext)

        config = MongoConfig()
    }

    @Test
    fun `should return user from JWT`() {
        securityContext.stub {
            on { securityContext.authentication } doReturn authentication
        }

        authentication.stub {
            on { isAuthenticated } doReturn true
            on { principal } doReturn jwt
        }

        jwt.stub {
            on { claims } doReturn mapOf(
                "user_name" to "user_name",
                "name" to "name",
                "email" to "email"
            )
        }

        val auditor = config.getCurrentAuditor()

        assertTrue(auditor.isPresent)
        val user = auditor.get()

        assertEquals("user_name", user.id)
        assertEquals("name", user.name)
        assertEquals("email", user.email)
    }


    @Test
    fun `should return empty on missing authentication`() {
        securityContext.stub {
            on { authentication } doReturn null
        }

        val auditor = config.getCurrentAuditor()

        assertTrue(auditor.isEmpty)
    }

    @Test
    fun `should return empty on missing user_name claim`() {
        securityContext.stub {
            on { securityContext.authentication } doReturn authentication
        }

        authentication.stub {
            on { isAuthenticated } doReturn true
            on { principal } doReturn jwt
        }

        jwt.stub {
            on { claims } doReturn emptyMap()
        }

        val auditor = config.getCurrentAuditor()

        assertTrue(auditor.isEmpty)
    }
}