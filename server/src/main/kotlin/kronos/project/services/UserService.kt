package kronos.project.services

import kronos.project.database.DatabaseFactory.dbQuery
import kronos.project.dto.SettingsDto
import kronos.project.dto.UpdateUserProfileRequest
import kronos.project.dto.UserProfileDto
import kronos.project.models.PinsTable
import kronos.project.models.UserSettingsTable
import kronos.project.models.UsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserService {
    fun getSettings(userId: UUID): SettingsDto = dbQuery {
        val row = UserSettingsTable.selectAll().where { UserSettingsTable.userId eq userId }.singleOrNull()
            ?: createDefaultSettings(userId)
        toSettings(row)
    }

    fun putSettings(userId: UUID, settings: SettingsDto): SettingsDto = dbQuery {
        val updated = UserSettingsTable.update({ UserSettingsTable.userId eq userId }) {
            it[darkMode] = settings.darkMode
            it[notificationsEnabled] = settings.notificationsEnabled
            it[language] = settings.language
        }
        if (updated == 0) {
            UserSettingsTable.insert {
                it[this.userId] = userId
                it[darkMode] = settings.darkMode
                it[notificationsEnabled] = settings.notificationsEnabled
                it[language] = settings.language
            }
        }
        getSettings(userId)
    }

    private fun createDefaultSettings(userId: UUID): ResultRow {
        UserSettingsTable.insert {
            it[this.userId] = userId
        }
        return UserSettingsTable.selectAll().where { UserSettingsTable.userId eq userId }.single()
    }

    fun getProfile(userId: UUID): UserProfileDto? = dbQuery {
        val user = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull() ?: return@dbQuery null
        val reports = PinsTable.selectAll().where { PinsTable.createdBy eq userId }.count().toInt()
        val resolved = PinsTable.selectAll().where { (PinsTable.createdBy eq userId) and (PinsTable.status eq kronos.project.models.PinStatus.RESOLVED) }.count().toInt()
        toProfile(user, reports, resolved)
    }

    fun updateProfile(userId: UUID, request: UpdateUserProfileRequest): UserProfileDto? = dbQuery {
        val updated = UsersTable.update({ UsersTable.id eq userId }) {
            it[firstName] = request.firstName
            it[lastName] = request.lastName
            it[role] = request.role
        }
        if (updated == 0) return@dbQuery null
        getProfile(userId)
    }

    private fun toSettings(row: ResultRow): SettingsDto = SettingsDto(
        darkMode = row[UserSettingsTable.darkMode],
        notificationsEnabled = row[UserSettingsTable.notificationsEnabled],
        language = row[UserSettingsTable.language],
    )

    private fun toProfile(row: ResultRow, reports: Int, resolved: Int): UserProfileDto = UserProfileDto(
        id = row[UsersTable.id].value.toString(),
        username = row[UsersTable.username],
        firstName = row[UsersTable.firstName],
        lastName = row[UsersTable.lastName],
        role = row[UsersTable.role],
        points = row[UsersTable.points],
        reports = reports,
        resolved = resolved,
    )
}

