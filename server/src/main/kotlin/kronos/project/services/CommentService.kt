package kronos.project.services

import kronos.project.database.DatabaseFactory.dbQuery
import kronos.project.dto.CommentResponse
import kronos.project.dto.CreateCommentRequest
import kronos.project.models.CommentVotesTable
import kronos.project.models.CommentsTable
import kronos.project.models.PinsTable
import kronos.project.models.UsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class CommentService {
    fun getCommentById(commentId: UUID): CommentResponse? = dbQuery {
        CommentsTable.selectAll().where { CommentsTable.id eq commentId }.singleOrNull()?.let { row ->
            toComment(row, emptyList())
        }
    }

    fun addComment(pinId: UUID, authorId: UUID, request: CreateCommentRequest): CommentResponse? = dbQuery {
        val pinExists = PinsTable.selectAll().where { PinsTable.id eq pinId }.empty().not()
        val userExists = UsersTable.selectAll().where { UsersTable.id eq authorId }.empty().not()
        if (!pinExists || !userExists) return@dbQuery null

        val parentUuid = request.parentId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        if (parentUuid != null) {
            val parentExists = CommentsTable.selectAll().where { CommentsTable.id eq parentUuid }.empty().not()
            if (!parentExists) return@dbQuery null
        }

        val commentId = CommentsTable.insert {
            it[this.pinId] = pinId
            it[this.authorId] = authorId
            it[this.parentId] = parentUuid
            it[content] = request.content
        }[CommentsTable.id].value

        CommentsTable.selectAll().where { CommentsTable.id eq commentId }.singleOrNull()?.let { row ->
            toComment(row, emptyList())
        }
    }

    fun listComments(pinId: UUID): List<CommentResponse> = dbQuery {
        val allRows = CommentsTable.selectAll()
            .where { CommentsTable.pinId eq pinId }
            .orderBy(CommentsTable.createdAt, SortOrder.ASC)
            .toList()

        buildTree(allRows, parentId = null)
    }

    fun voteOnComment(commentId: UUID, userId: UUID, isUpvote: Boolean): Boolean = dbQuery {
        val commentExists = CommentsTable.selectAll().where { CommentsTable.id eq commentId }.empty().not()
        val userExists = UsersTable.selectAll().where { UsersTable.id eq userId }.empty().not()
        if (!commentExists || !userExists) return@dbQuery false

        val existingVote = CommentVotesTable.selectAll()
            .where { (CommentVotesTable.commentId eq commentId) and (CommentVotesTable.userId eq userId) }
            .singleOrNull()

        if (existingVote == null) {
            CommentVotesTable.insert {
                it[this.commentId] = commentId
                it[this.userId] = userId
                it[this.isUpvote] = isUpvote
            }
            if (isUpvote) {
                CommentsTable.update({ CommentsTable.id eq commentId }) {
                    it[upvotes] = upvotes + 1
                }
            } else {
                CommentsTable.update({ CommentsTable.id eq commentId }) {
                    it[downvotes] = downvotes + 1
                }
            }
        } else {
            val previousIsUpvote = existingVote[CommentVotesTable.isUpvote]
            if (previousIsUpvote == isUpvote) {
                // Remove vote (toggle off)
                CommentVotesTable.deleteWhere {
                    (CommentVotesTable.commentId eq commentId) and (CommentVotesTable.userId eq userId)
                }
                if (isUpvote) {
                    CommentsTable.update({ CommentsTable.id eq commentId }) {
                        it[upvotes] = upvotes - 1
                    }
                } else {
                    CommentsTable.update({ CommentsTable.id eq commentId }) {
                        it[downvotes] = downvotes - 1
                    }
                }
            } else {
                // Change vote direction
                CommentVotesTable.update({
                    (CommentVotesTable.commentId eq commentId) and (CommentVotesTable.userId eq userId)
                }) {
                    it[this.isUpvote] = isUpvote
                }
                if (isUpvote) {
                    CommentsTable.update({ CommentsTable.id eq commentId }) {
                        it[upvotes] = upvotes + 1
                        it[downvotes] = downvotes - 1
                    }
                } else {
                    CommentsTable.update({ CommentsTable.id eq commentId }) {
                        it[downvotes] = downvotes + 1
                        it[upvotes] = upvotes - 1
                    }
                }
            }
        }

        true
    }

    fun deleteComment(commentId: UUID): Boolean = dbQuery {
        CommentsTable.deleteWhere { CommentsTable.id eq commentId } > 0
    }

    private fun buildTree(rows: List<ResultRow>, parentId: UUID?): List<CommentResponse> {
        return rows
            .filter { row -> row[CommentsTable.parentId]?.value == parentId }
            .map { row ->
                val id = row[CommentsTable.id].value
                val children = buildTree(rows, id)
                toComment(row, children)
            }
    }

    private fun toComment(row: ResultRow, replies: List<CommentResponse>): CommentResponse = CommentResponse(
        id = row[CommentsTable.id].value.toString(),
        pinId = row[CommentsTable.pinId].value.toString(),
        parentId = row[CommentsTable.parentId]?.value?.toString(),
        authorId = row[CommentsTable.authorId].value.toString(),
        content = row[CommentsTable.content],
        createdAt = row[CommentsTable.createdAt].toString(),
        upvotes = row[CommentsTable.upvotes],
        downvotes = row[CommentsTable.downvotes],
        replies = replies,
    )
}

