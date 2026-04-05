package kronos.project.presentation

import androidx.lifecycle.ViewModel
import kronos.project.Dependencies
import kronos.project.domain.model.UserRole

class CreateIssueViewModel : ViewModel() {
    suspend fun createIssue(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        val createdBy = requireNotNull(Dependencies.currentUserId.value) {
            "Missing authenticated user id"
        }
        Dependencies.pinRepository
            .createPin(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                category = category,
                createdBy = createdBy,
            )
            .getOrThrow()

        if (Dependencies.currentUserRole.value == UserRole.CITIZEN) {
            Dependencies.awardPointsForIssue(10)
        }
    }
}
