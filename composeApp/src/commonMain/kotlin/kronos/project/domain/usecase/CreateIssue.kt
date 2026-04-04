package kronos.project.domain.usecase

import kronos.project.domain.model.Issue
import kronos.project.domain.repository.IssueRepository

class CreateIssue(private val repository: IssueRepository) {
    suspend operator fun invoke(issue: Issue) = repository.createIssue(issue)
}
