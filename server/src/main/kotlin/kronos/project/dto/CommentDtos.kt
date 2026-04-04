package kronos.project.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequest(
    val authorId: String,
    val content: String,
    val parentId: String? = null,
)

@Serializable
data class CommentResponse(
    val id: String,
    val pinId: String,
    val parentId: String? = null,
    val authorId: String,
    val content: String,
    val createdAt: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val replies: List<CommentResponse> = emptyList(),
)

@Serializable
data class VoteCommentRequest(
    val userId: String,
    val isUpvote: Boolean,
)

