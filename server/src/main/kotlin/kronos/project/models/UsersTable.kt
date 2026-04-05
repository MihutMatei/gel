package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UsersTable : UUIDTable("users") {
    val username = varchar("username", length = 64).uniqueIndex()
    val email = varchar("email", length = 255).uniqueIndex()
    // Migration note for existing DBs: ALTER TABLE users ADD COLUMN first_name VARCHAR(100) NOT NULL DEFAULT '';
    // ALTER TABLE users ADD COLUMN last_name VARCHAR(100) NOT NULL DEFAULT '';
    // ALTER TABLE users ADD COLUMN points INT NOT NULL DEFAULT 0;
    // ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'CITIZEN';
    val firstName = varchar("first_name", length = 100).default("")
    val lastName = varchar("last_name", length = 100).default("")
    val points = integer("points").default(0)
    val role = varchar("role", length = 32).default("CITIZEN")
    // Migration note for existing DBs: ALTER TABLE users ADD COLUMN password_hash TEXT NOT NULL DEFAULT '';
    // then backfill secure hashes and remove the temporary default if needed.
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

