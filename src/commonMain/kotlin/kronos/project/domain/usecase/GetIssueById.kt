package kronos.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import kronos.project.domain.model.Issue
import kronos.project.domain.repository.IssueRepository

class GetIssueById(private val repository: IssueRepository) {
    operator fun invoke(id: String): Flow<Issue?> = repository.getIssueById(id)
}
