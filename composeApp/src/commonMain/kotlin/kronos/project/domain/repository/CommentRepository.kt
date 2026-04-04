package kronos.project.domain.repository

import kotlinx.coroutines.flow.Flow
import kronos.project.domain.model.Comment

interface CommentRepository {
    fun getCommentsForIssue(issueId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
}
