package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.dto.CreatePinRequestDto
import kronos.project.data.remote.dto.PinDto

class PinRepository(private val httpClient: HttpClient) {
    suspend fun fetchPins(): Result<List<PinDto>> = runCatching {
        httpClient.get("${ApiConfig.BASE_URL}/pins").body<List<PinDto>>()
    }

    suspend fun createPin(
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        category: String,
        createdBy: String,
    ): Result<PinDto> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/pins") {
            contentType(ContentType.Application.Json)
            setBody(
                CreatePinRequestDto(
                    title = title,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    category = category,
                    createdBy = createdBy,
                ),
            )
        }.body<PinDto>()
    }
}

