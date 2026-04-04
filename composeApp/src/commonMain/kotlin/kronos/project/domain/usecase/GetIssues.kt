package kronos.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import kronos.project.domain.model.Issue
import kronos.project.domain.repository.IssueRepository

class GetIssues(private val repository: IssueRepository) {
    operator fun invoke(): Flow<List<Issue>> = repository.getIssues()
}
