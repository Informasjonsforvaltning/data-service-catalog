package no.fdk.catalog.common.history

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "history")
internal data class History(

    @Id
    val id: String,

    @CreatedDate
    val created: LocalDateTime? = null,

    @LastModifiedDate
    val modified: LocalDateTime? = null,

    @Version
    val version: Int? = null,
)

