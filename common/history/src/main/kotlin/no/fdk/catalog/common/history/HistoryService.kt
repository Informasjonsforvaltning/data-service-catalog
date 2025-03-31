package no.fdk.catalog.common.history

interface HistoryService {
    fun save(history: HistoryDTO)
    fun findAll(): List<HistoryDTO>
}