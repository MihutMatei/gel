package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kronos.project.Dependencies
import kronos.project.domain.model.Issue
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole

@Suppress("DEPRECATION_ERROR")
private fun nowInstant(): Instant = Instant.now()

class CreateIssueViewModel : ViewModel() {
    suspend fun createIssue(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        val newIssue = Issue(
            id = nowInstant().toEpochMilliseconds().toString(),
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            status = IssueStatus.OPEN,
            authorRole = Dependencies.currentUserRole.value,
            createdAt = nowInstant()
        )
        Dependencies.createIssue(newIssue)
        if (Dependencies.currentUserRole.value == UserRole.CITIZEN) {
            Dependencies.awardPointsForIssue(10)
        }
    }
}
