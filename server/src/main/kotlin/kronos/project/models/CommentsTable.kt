package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object CommentsTable : UUIDTable("comments") {
    val pinId = reference("pin_id", PinsTable, onDelete = ReferenceOption.CASCADE)
    val authorId = reference("author_id", UsersTable, onDelete = ReferenceOption.RESTRICT)
    val parentId = optReference("parent_id", CommentsTable, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val upvotes = integer("upvotes").default(0)
    val downvotes = integer("downvotes").default(0)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

