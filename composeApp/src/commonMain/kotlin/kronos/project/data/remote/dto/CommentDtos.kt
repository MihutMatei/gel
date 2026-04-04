package kronos.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    val pinId: String,
    val parentId: String? = null,
    val authorId: String,
    val content: String,
    val createdAt: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val replies: List<CommentDto> = emptyList(),
)

@Serializable
data class CreateCommentDto(
    val authorId: String,
    val content: String,
    val parentId: String? = null,
)

@Serializable
data class VoteCommentDto(
    val userId: String,
    val isUpvote: Boolean,
)

@Serializable
data class CreatePinDto(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val createdBy: String,
)
