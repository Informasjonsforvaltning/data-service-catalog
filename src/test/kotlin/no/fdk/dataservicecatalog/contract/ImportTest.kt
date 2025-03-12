package no.fdk.dataservicecatalog.contract

import no.fdk.dataservicecatalog.domain.DataService
import no.fdk.dataservicecatalog.domain.ImportResult
import no.fdk.dataservicecatalog.integration.MongoDBTestcontainer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals

@Tag("contract")
@ActiveProfiles("test")

@AutoConfigureMockMvc
@Import(MongoDBTestcontainer::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ImportTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var operations: MongoOperations

    @AfterEach
    fun cleanup() {
        operations.remove(Query(), DataService::class.java)
        operations.remove(Query(), ImportResult::class.java)
    }

    @Test
    fun `import openAPI should respond with created and location`() {
        val specification = """
            {
                "openapi": "3.0.0",
                "info": {
                    "version": "1.0",
                    "title": "title",
                    "description": "description",
                    "termsOfService": "https://example.com/tos",
                    "contact": {
                        "name": "contact",
                        "email": "invalid",
                        "url": "https://example.com/contact"
                    }
                },
                "servers": [
                    {
                        "url": "https://example.com"
                    }, 
                    {
                        "url": "https://text.example.com"
                    }
                ],
                "tags": [
                    {
                        "name": "tag"
                    }
                ],
                "externalDocs": {
                    "url": "https://example.com/docs"
                }
            }
        """.trimIndent()

        val catalogId = "1234"

        mockMvc.post("/internal/catalogs/$catalogId/import") {
            contentType = MediaType.APPLICATION_JSON
            content = specification
            with(jwt().authorities(SimpleGrantedAuthority("organization:%s:admin".format(catalogId))))
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
        }

        assertEquals(2, operations.count(Query(), DataService::class.java))
        assertEquals(1, operations.count(Query(), ImportResult::class.java))
    }
}
