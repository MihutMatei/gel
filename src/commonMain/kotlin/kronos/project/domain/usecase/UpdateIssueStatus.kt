package kronos.project.domain.usecase

import kronos.project.domain.model.IssueStatus
import kronos.project.domain.repository.IssueRepository

class UpdateIssueStatus(private val repository: IssueRepository) {
    suspend operator fun invoke(id: String, status: IssueStatus) = repository.updateIssueStatus(id, status)
}
