package kronos.project.services

import kronos.project.database.DatabaseFactory.dbQuery
import kronos.project.dto.AuthUserResponse
import kronos.project.dto.LoginRequest
import kronos.project.dto.RegisterRequest
import kronos.project.models.UsersTable
import kronos.project.security.Hashing
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

sealed class RegisterResult {
    data class Success(val user: AuthUserResponse) : RegisterResult()
    data object UsernameTaken : RegisterResult()
    data object EmailTaken : RegisterResult()
    data object Failed : RegisterResult()
}

class AuthService {
    fun register(request: RegisterRequest): RegisterResult = runCatching {
        dbQuery {
            val existingByUsername = UsersTable.selectAll().where { UsersTable.username eq request.username }.empty().not()
            if (existingByUsername) return@dbQuery RegisterResult.UsernameTaken

            val existingByEmail = UsersTable.selectAll().where { UsersTable.email eq request.email }.empty().not()
            if (existingByEmail) return@dbQuery RegisterResult.EmailTaken

            val userId = UsersTable.insert {
                it[username] = request.username
                it[email] = request.email
                it[passwordHash] = Hashing.hashPassword(request.password)
            }[UsersTable.id].value

            val created = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
                ?: return@dbQuery RegisterResult.Failed

            RegisterResult.Success(toUserResponse(created))
        }
    }.getOrElse { RegisterResult.Failed }

    fun login(request: LoginRequest): AuthUserResponse? = runCatching {
        dbQuery {
            val row = UsersTable.selectAll().where { UsersTable.email eq request.email }.singleOrNull()
                ?: return@dbQuery null

            val isValid = Hashing.verifyPassword(request.password, row[UsersTable.passwordHash])
            if (!isValid) return@dbQuery null

            toUserResponse(row)
        }
    }.getOrNull()

    fun findById(userId: UUID): AuthUserResponse? = runCatching {
        dbQuery {
            UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()?.let(::toUserResponse)
        }
    }.getOrNull()

    private fun toUserResponse(row: ResultRow): AuthUserResponse = AuthUserResponse(
        id = row[UsersTable.id].value.toString(),
        username = row[UsersTable.username],
        email = row[UsersTable.email],
    )
}

