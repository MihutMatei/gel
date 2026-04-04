package kronos.project.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object CommentVotesTable : UUIDTable("comment_votes") {
    val commentId = reference("comment_id", CommentsTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val isUpvote = bool("is_upvote")

    init {
        uniqueIndex("comment_votes_comment_user_unique", commentId, userId)
    }
}
