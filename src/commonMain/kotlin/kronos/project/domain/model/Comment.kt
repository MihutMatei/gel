package kronos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val issueId: String,
    val text: String,
    val authorRole: UserRole,
    val createdAt: Instant
)
