package kronos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val status: IssueStatus,
    val authorRole: UserRole,
    val createdAt: Instant
)
