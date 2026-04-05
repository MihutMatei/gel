package kronos.project.domain.usecase

import kronos.project.domain.model.Comment
import kronos.project.domain.repository.CommentRepository

class AddComment(private val repository: CommentRepository) {
    suspend operator fun invoke(comment: Comment) = repository.addComment(comment)
}
