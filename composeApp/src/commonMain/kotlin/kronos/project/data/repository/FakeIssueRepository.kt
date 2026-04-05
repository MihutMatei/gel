package kronos.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kronos.project.domain.model.Issue
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole
import kronos.project.domain.repository.IssueRepository

class FakeIssueRepository : IssueRepository {
    private val _issues = MutableStateFlow<List<Issue>>(
        listOf(
            Issue(
                id = "1",
                title = "Broken Bus Stop",
                description = "The glass is shattered at the main square bus stop.",
                category = "public_transport",
                latitude = 44.4268,
                longitude = 26.1025,
                status = IssueStatus.OPEN,
                authorRole = UserRole.CITIZEN,
                createdAt = Clock.System.now()
            ),
            Issue(
                id = "2",
                title = "Water Leak",
                description = "Major water leak on the sidewalk.",
                category = "utilities",
                latitude = 44.4396,
                longitude = 26.0963,
                status = IssueStatus.IN_PROGRESS,
                authorRole = UserRole.CITIZEN,
                createdAt = Clock.System.now()
            ),
            Issue(
                id = "3",
                title = "Illegal Parking",
                description = "Cars parked on the bike lane.",
                category = "parking",
                latitude = 44.4323,
                longitude = 26.1067,
                status = IssueStatus.OPEN,
                authorRole = UserRole.CITIZEN,
                createdAt = Clock.System.now()
            ),
            Issue(
                id = "4",
                title = "Street Light Out",
                description = "Dark alley near the park.",
                category = "crime_safety",
                latitude = 44.4412,
                longitude = 26.1150,
                status = IssueStatus.RESOLVED,
                authorRole = UserRole.TOWNHALL_EMPLOYEE,
                createdAt = Clock.System.now()
            )
        )
    )

    override fun getIssues(): Flow<List<Issue>> = _issues.asStateFlow()

    override fun getIssueById(id: String): Flow<Issue?> = _issues.map { list ->
        list.find { it.id == id }
    }

    override suspend fun createIssue(issue: Issue) {
        _issues.value = _issues.value + issue
    }

    override suspend fun updateIssueStatus(id: String, status: IssueStatus) {
        _issues.value = _issues.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
    }
}
