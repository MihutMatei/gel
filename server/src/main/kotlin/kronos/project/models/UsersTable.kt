package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UsersTable : UUIDTable("users") {
    val username = varchar("username", length = 64).uniqueIndex()
    val email = varchar("email", length = 255).uniqueIndex()
    // Migration note for existing DBs: ALTER TABLE users ADD COLUMN password_hash TEXT NOT NULL DEFAULT '';
    // then backfill secure hashes and remove the temporary default if needed.
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

