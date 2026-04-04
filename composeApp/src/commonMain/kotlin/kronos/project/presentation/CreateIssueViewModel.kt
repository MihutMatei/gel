package kronos.project.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kronos.project.Dependencies
import kronos.project.data.remote.dto.CreatePinDto
import kronos.project.domain.model.Issue
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole

class CreateIssueViewModel : ViewModel() {
    private val pinRepository = Dependencies.pinRepository

    suspend fun createIssue(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        val userId = Dependencies.currentUser.value?.id

        if (userId != null) {
            val request = CreatePinDto(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                category = category,
                createdBy = userId,
            )
            pinRepository.createPin(request).getOrThrow()
        } else {
            // Fallback: create issue in local fake repository when not authenticated
            val newIssue = Issue(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                title = title,
                description = description,
                category = category,
                latitude = latitude,
                longitude = longitude,
                status = IssueStatus.OPEN,
                authorRole = Dependencies.currentUserRole.value,
                createdAt = Clock.System.now()
            )
            Dependencies.createIssue(newIssue)
            if (Dependencies.currentUserRole.value == UserRole.CITIZEN) {
                Dependencies.awardPointsForIssue(10)
            }
        }
    }
}
