package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kronos.project.Dependencies
import kronos.project.domain.model.Issue
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole
import kronos.project.util.newIdFromTime
import kronos.project.util.nowInstant

class CreateIssueViewModel : ViewModel() {
    suspend fun createIssue(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        val newIssue = Issue(
            id = newIdFromTime(),
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
