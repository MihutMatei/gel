package kronos.project

import kronos.project.data.repository.FakeCommentRepository
import kronos.project.data.repository.FakeGamificationRepository
import kronos.project.data.repository.FakeIssueRepository
import kronos.project.domain.model.UserRole
import kronos.project.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow

object Dependencies {
    val currentUserRole = MutableStateFlow(UserRole.CITIZEN)
    val isDarkMode = MutableStateFlow<Boolean?>(null) // null means follow system

    val issueRepository = FakeIssueRepository()
    val commentRepository = FakeCommentRepository()
    val gamificationRepository = FakeGamificationRepository()

    val getIssues = GetIssues(issueRepository)
    val getIssueById = GetIssueById(issueRepository)
    val createIssue = CreateIssue(issueRepository)
    val updateIssueStatus = UpdateIssueStatus(issueRepository)
    val addComment = AddComment(commentRepository)
    val awardPointsForIssue = AwardPointsForIssue(gamificationRepository)
}
