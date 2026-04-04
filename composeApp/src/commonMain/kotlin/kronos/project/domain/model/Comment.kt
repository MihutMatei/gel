package kronos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val issueId: String,
    val parentId: String? = null,
    val text: String,
    val authorRole: UserRole,
    val createdAt: Instant,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val replies: List<Comment> = emptyList(),
)
