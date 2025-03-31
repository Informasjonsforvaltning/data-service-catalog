package no.fdk.catalog.dataservice.config

import no.fdk.catalog.dataservice.core.domain.DataService
import no.fdk.catalog.dataservice.core.domain.User
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = ["no.fdk.catalog.dataservice", "no.fdk.catalog.common.history"])
class MongoConfig : AuditorAware<User> {
    private val logger: Logger = LoggerFactory.getLogger(MongoConfig::class.java)

    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(dbFactory)
    }

    @Bean
    fun configureIndexes(mongoOperations: MongoOperations): Boolean {
        val indexOps = mongoOperations.indexOps(DataService::class.java)

        indexOps.ensureIndex(Index().on("catalogId", Sort.Direction.ASC).on("published", Sort.Direction.ASC))

        indexOps.ensureIndex(CompoundIndexDefinition(Document().append("catalogId", 1).append("published", 1)))

        return true
    }

    override fun getCurrentAuditor(): Optional<User> {
        val user = SecurityContextHolder.getContext()
            ?.authentication
            ?.takeIf { it.isAuthenticated }
            ?.let { it.principal as? Jwt }
            ?.let { jwt ->
                jwt.claims["user_name"]
                    ?.let { it as? String }
                    .also { if (it == null) logger.error("user_name claim missing in token") }
                    ?.let { userName ->
                        User(
                            id = userName,
                            name = jwt.claims["name"] as? String,
                            email = jwt.claims["email"] as? String,
                        )
                    }
            }

        return Optional.ofNullable(user)
    }
}
