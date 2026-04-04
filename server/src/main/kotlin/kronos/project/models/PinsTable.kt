package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

enum class PinStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
}

object PinsTable : UUIDTable("pins") {
    val title = varchar("title", length = 160)
    val description = text("description")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val category = varchar("category", length = 64)
    val status = enumerationByName("status", length = 32, klass = PinStatus::class)
    val createdBy = reference("created_by", UsersTable, onDelete = ReferenceOption.RESTRICT)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

