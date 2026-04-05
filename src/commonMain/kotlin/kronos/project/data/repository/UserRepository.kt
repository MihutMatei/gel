package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.dto.ErrorResponseDto
import kronos.project.data.remote.dto.UpdateUserProfileRequestDto
import kronos.project.data.remote.dto.UserProfileDto
import kronos.project.domain.model.UserProfile
import kronos.project.domain.model.UserRole

class UserRepository(private val httpClient: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMyProfile(): Result<UserProfile> = runCatching {
        val response = httpClient.get("${ApiConfig.BASE_URL}/users/me/profile")
        if (!response.status.isSuccess()) throw IllegalStateException(parseMessage(response.bodyAsText()))
        response.body<UserProfileDto>().toDomain()
    }

    suspend fun putMyProfile(profile: UserProfile): Result<UserProfile> = runCatching {
        val response = httpClient.put("${ApiConfig.BASE_URL}/users/me/profile") {
            contentType(ContentType.Application.Json)
            setBody(
                UpdateUserProfileRequestDto(
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    role = profile.role.name,
                ),
            )
        }
        if (!response.status.isSuccess()) throw IllegalStateException(parseMessage(response.bodyAsText()))
        response.body<UserProfileDto>().toDomain()
    }

    private fun parseMessage(raw: String): String {
        val parsed = runCatching { json.decodeFromString<ErrorResponseDto>(raw).message }.getOrNull()
        return parsed?.takeIf { it.isNotBlank() } ?: "Request failed"
    }
}

private fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    username = username,
    firstName = firstName,
    lastName = lastName,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.CITIZEN),
    points = points,
    reports = reports,
    resolved = resolved,
)

