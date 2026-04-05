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
import kronos.project.domain.model.SettingsDto

class SettingsRepository(private val httpClient: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMySettings(): Result<SettingsDto> = runCatching {
        val response = httpClient.get("${ApiConfig.BASE_URL}/users/me/settings")
        if (!response.status.isSuccess()) throw IllegalStateException(parseMessage(response.bodyAsText()))
        response.body<SettingsDto>()
    }

    suspend fun putMySettings(settings: SettingsDto): Result<SettingsDto> = runCatching {
        val response = httpClient.put("${ApiConfig.BASE_URL}/users/me/settings") {
            contentType(ContentType.Application.Json)
            setBody(settings)
        }
        if (!response.status.isSuccess()) throw IllegalStateException(parseMessage(response.bodyAsText()))
        response.body<SettingsDto>()
    }

    private fun parseMessage(raw: String): String {
        val parsed = runCatching { json.decodeFromString<ErrorResponseDto>(raw).message }.getOrNull()
        return parsed?.takeIf { it.isNotBlank() } ?: "Request failed"
    }
}

