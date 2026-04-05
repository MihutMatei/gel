package kronos.project.domain.repository

import kotlinx.coroutines.flow.Flow
import kronos.project.domain.model.Issue
import kronos.project.domain.model.IssueStatus

interface IssueRepository {
    fun getIssues(): Flow<List<Issue>>
    fun getIssueById(id: String): Flow<Issue?>
    suspend fun createIssue(issue: Issue)
    suspend fun updateIssueStatus(id: String, status: IssueStatus)
}
