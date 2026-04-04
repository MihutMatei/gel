package kronos.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kronos.project.domain.model.Comment
import kronos.project.domain.model.UserRole
import kronos.project.domain.repository.CommentRepository

class FakeCommentRepository : CommentRepository {
    private val _comments = MutableStateFlow<List<Comment>>(
        listOf(
            Comment(
                id = "c1",
                issueId = "1",
                text = "I saw this too, it's dangerous for kids.",
                authorRole = UserRole.CITIZEN,
                createdAt = Clock.System.now()
            ),
            Comment(
                id = "c2",
                issueId = "1",
                text = "We have dispatched a team to clean up.",
                authorRole = UserRole.TOWNHALL_EMPLOYEE,
                createdAt = Clock.System.now()
            )
        )
    )

    override fun getCommentsForIssue(issueId: String): Flow<List<Comment>> = _comments.map { list ->
        list.filter { it.issueId == issueId }
    }

    override suspend fun addComment(comment: Comment) {
        _comments.value = _comments.value + comment
    }
}
