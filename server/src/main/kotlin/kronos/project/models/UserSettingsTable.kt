package kronos.project.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserSettingsTable : Table("user_settings") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val darkMode = bool("dark_mode").default(false)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    val language = varchar("language", length = 16).default("en")

    override val primaryKey = PrimaryKey(userId)
}

