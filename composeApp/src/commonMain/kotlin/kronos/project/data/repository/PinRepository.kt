package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.dto.CreatePinCommentRequestDto
import kronos.project.data.remote.dto.CreatePinRequestDto
import kronos.project.data.remote.dto.PinCommentDto
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

    suspend fun fetchPinComments(pinId: String): Result<List<PinCommentDto>> = runCatching {
        httpClient.get("${ApiConfig.BASE_URL}/pins/$pinId/comments").body<List<PinCommentDto>>()
    }

    suspend fun createPinComment(
        pinId: String,
        authorId: String,
        content: String,
    ): Result<PinCommentDto> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/pins/$pinId/comments") {
            contentType(ContentType.Application.Json)
            setBody(
                CreatePinCommentRequestDto(
                    authorId = authorId,
                    content = content,
                ),
            )
        }.body<PinCommentDto>()
    }
}

