package no.fdk.dataservicecatalog.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "data_services")
data class DataServiceEntity(
    @Id
    @Column(name = "id", nullable = false, length = 255)
    val id: String,
    @Column(name = "catalog_id", nullable = false, length = 50)
    val catalogId: String,
    @Column(name = "published", nullable = false)
    val published: Boolean = false,
    @Column(name = "published_date", nullable = true)
    val publishedDate: LocalDateTime? = null,

    /**
     * The JSON representation of the data service values.
     *
     * This field contains the structured JSON data that represents the
     * registered input values for the data service.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    val data: Map<String, Any>? = null,
)
