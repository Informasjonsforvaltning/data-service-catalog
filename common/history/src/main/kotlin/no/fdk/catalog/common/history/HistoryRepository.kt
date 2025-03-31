package no.fdk.catalog.common.history

import org.springframework.data.mongodb.repository.MongoRepository

internal interface HistoryRepository : MongoRepository<History, String> {
}
