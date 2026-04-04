package kronos.project.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Complaint(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val comments: List<ComplaintComment> = emptyList(),
)

@Serializable
data class ComplaintComment(
    val id: String,
    val complaintId: String,
    val parentId: String? = null,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val replies: List<ComplaintComment> = emptyList(),
)

@Serializable
enum class VoteType {
    UP,
    DOWN,
}

@Serializable
data class VoteRequest(
    val userId: String,
    val voteType: VoteType,
)
