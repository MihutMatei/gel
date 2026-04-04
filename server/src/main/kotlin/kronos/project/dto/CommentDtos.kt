package kronos.project.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequest(
    val authorId: String,
    val content: String,
)

@Serializable
data class CommentResponse(
    val id: String,
    val pinId: String,
    val authorId: String,
    val content: String,
    val createdAt: String,
)

