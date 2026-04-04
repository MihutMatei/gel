package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object CommentsTable : UUIDTable("comments") {
    val pinId = reference("pin_id", PinsTable, onDelete = ReferenceOption.CASCADE)
    val authorId = reference("author_id", UsersTable, onDelete = ReferenceOption.RESTRICT)
    val content = text("content")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

