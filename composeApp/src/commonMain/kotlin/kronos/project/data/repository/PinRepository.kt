package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.dto.CommentDto
import kronos.project.data.remote.dto.CreateCommentDto
import kronos.project.data.remote.dto.CreatePinDto
import kronos.project.data.remote.dto.PinDto
import kronos.project.data.remote.dto.VoteCommentDto

class PinRepository(private val httpClient: HttpClient) {
    suspend fun fetchPins(): Result<List<PinDto>> = runCatching {
        httpClient.get("${ApiConfig.BASE_URL}/pins").body<List<PinDto>>()
    }

    suspend fun createPin(request: CreatePinDto): Result<PinDto> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/pins") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<PinDto>()
    }

    suspend fun fetchComments(pinId: String): Result<List<CommentDto>> = runCatching {
        httpClient.get("${ApiConfig.BASE_URL}/pins/$pinId/comments").body<List<CommentDto>>()
    }

    suspend fun addComment(pinId: String, request: CreateCommentDto): Result<CommentDto> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/pins/$pinId/comments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<CommentDto>()
    }

    suspend fun replyToComment(commentId: String, request: CreateCommentDto): Result<CommentDto> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/comments/$commentId/reply") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<CommentDto>()
    }

    suspend fun voteOnComment(commentId: String, request: VoteCommentDto): Result<Unit> = runCatching {
        httpClient.post("${ApiConfig.BASE_URL}/comments/$commentId/vote") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        Unit
    }
}

