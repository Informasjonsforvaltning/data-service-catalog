package no.fdk.catalog.common.history

import org.springframework.stereotype.Service
import java.util.*

@Service
internal class HistoryServiceImpl(private val repository: HistoryRepository) : HistoryService {

    override fun save(history: HistoryDTO) {
        repository.save(History(id = UUID.randomUUID().toString()))
    }

    override fun findAll(): List<HistoryDTO> {
        return repository.findAll().map { HistoryDTO(id = it.id) }
    }
}
