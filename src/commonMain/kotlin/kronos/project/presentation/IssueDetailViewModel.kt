package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kronos.project.Dependencies
import kronos.project.domain.model.Comment
import kronos.project.domain.model.IssueStatus

@Suppress("DEPRECATION_ERROR")
private fun nowInstant(): Instant = Instant.now()

class IssueDetailViewModel(val issueId: String) : ViewModel() {
    val issue = Dependencies.getIssueById(issueId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val comments = Dependencies.commentRepository.getCommentsForIssue(issueId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserRole = Dependencies.currentUserRole

    fun updateStatus(status: IssueStatus) {
        viewModelScope.launch {
            Dependencies.updateIssueStatus(issueId, status)
        }
    }

    fun addComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val comment = Comment(
                id = nowInstant().toEpochMilliseconds().toString(),
                issueId = issueId,
                text = text,
                authorRole = currentUserRole.value,
                createdAt = nowInstant()
            )
            Dependencies.addComment(comment)
        }
    }
}
