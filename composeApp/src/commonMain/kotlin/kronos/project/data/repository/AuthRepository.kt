package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.TokenStorage
import kronos.project.data.remote.dto.AuthUserDto
import kronos.project.data.remote.dto.ErrorResponseDto
import kronos.project.data.remote.dto.LoginRequestDto
import kronos.project.data.remote.dto.LoginResponseDto
import kronos.project.data.remote.dto.RegisterRequestDto
import kronos.project.domain.model.AuthUser

class AuthRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun register(username: String, firstName: String, lastName: String, email: String, password: String): Result<AuthUser> = runCatching {
        val response = httpClient.post("${ApiConfig.BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequestDto(
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                ),
            )
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException(parseErrorBody(response.bodyAsText()))
        }

        val user = response.body<AuthUserDto>()

        user.toDomain()
    }

    suspend fun login(email: String, password: String): Result<AuthUser> = runCatching {
        val response = httpClient.post("${ApiConfig.BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password))
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException(parseErrorBody(response.bodyAsText()))
        }

        val payload = response.body<LoginResponseDto>()

        tokenStorage.setToken(payload.accessToken)
        payload.user.toDomain()
    }

    suspend fun me(): Result<AuthUser> = runCatching {
        val response = httpClient.get("${ApiConfig.BASE_URL}/auth/me")
        if (!response.status.isSuccess()) {
            throw IllegalStateException(parseErrorBody(response.bodyAsText()))
        }
        response.body<AuthUserDto>().toDomain()
    }

    fun isLoggedIn(): Boolean = !tokenStorage.token().isNullOrBlank()

    fun logout() {
        tokenStorage.clear()
    }

    fun mapErrorMessage(throwable: Throwable): String {
        return throwable.message?.takeIf { it.isNotBlank() } ?: "Request failed"
    }

    private fun parseErrorBody(raw: String): String {
        val parsed = runCatching { json.decodeFromString<ErrorResponseDto>(raw).message }.getOrNull()
        return parsed?.takeIf { it.isNotBlank() } ?: "Request failed"
    }
}

private fun AuthUserDto.toDomain(): AuthUser = AuthUser(
    id = id,
    username = username,
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    email = email,
)

