package no.fdk.dataservicecatalog.integration.repository

import no.fdk.dataservicecatalog.entity.DataServiceEntity
import no.fdk.dataservicecatalog.integration.PostgresDBTestcontainer
import no.fdk.dataservicecatalog.repository.DataServiceRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@ActiveProfiles("test")
@Import(PostgresDBTestcontainer::class)
@SpringBootTest
class DataServiceRepositoryTest(
    @param:Autowired val repository: DataServiceRepository
) {

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `find all by catalog id`() {
        val firstCatalogId = "123"
        val secondCatalogId = "456"

        val dataService = DataServiceEntity(
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            data = emptyMap()
        )

        repository.saveAll(
            listOf(dataService, dataService.copy(id = "2222", catalogId = secondCatalogId))
        )

        val dataServices = repository.findAllByCatalogId(firstCatalogId)

        assertEquals(1, dataServices.count())
        assertEquals(firstCatalogId, dataServices.first().catalogId)
    }

    @Test
    fun `find all by catalog ids in`() {
        val firstCatalogId = "123"
        val secondCatalogId = "456"

        val dataService = DataServiceEntity(
            id = "1111",
            catalogId = firstCatalogId,
            published = true,
            data = emptyMap()
        )

        repository.saveAll(
            listOf(dataService, dataService.copy(id = "2222", catalogId = secondCatalogId))
        )

        val dataServices = repository.findAllByCatalogIdIn(setOf(firstCatalogId))

        assertEquals(1, dataServices.count())
        assertEquals(firstCatalogId, dataServices.first().catalogId)
    }

    @Test
    fun `find by data service id`() {
        val catalogId = "1234"
        val dataServiceId = "5678"

        repository.save(
            DataServiceEntity(
                id = dataServiceId,
                catalogId = catalogId,
                published = true,
                data = emptyMap()
            )
        )

        val dataService = repository.findDataServiceById(dataServiceId)

        assertEquals(dataServiceId, dataService?.id)
    }

    @Test
    fun `find all by published status`() {
        val catalogId = "1234"

        val dataService = DataServiceEntity(
            id = "1111",
            catalogId = catalogId,
            published = true,
            data = emptyMap()
        )

        repository.saveAll(
            listOf(dataService, dataService.copy(id = "2222", published = false))
        )

        val dataServices = repository.findAllByPublished(true)

        assertEquals(1, dataServices.count())
        assertEquals(true, dataServices.first().published)
    }

    @Test
    fun `find all by catalog id and published status`() {
        val catalogId = "1234"

        val dataService = DataServiceEntity(
            id = "1111",
            catalogId = catalogId,
            published = true,
            data = emptyMap()
        )

        repository.saveAll(
            listOf(dataService, dataService.copy(id = "2222", published = false))
        )

        val dataServices = repository.findAllByCatalogIdAndPublished(catalogId, true)

        assertEquals(1, dataServices.count())
        assertEquals(true, dataServices.first().published)
    }
}
