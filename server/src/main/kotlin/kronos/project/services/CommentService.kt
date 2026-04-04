package kronos.project.services

import kronos.project.database.DatabaseFactory.dbQuery
import kronos.project.dto.CommentResponse
import kronos.project.dto.CreateCommentRequest
import kronos.project.models.CommentsTable
import kronos.project.models.PinsTable
import kronos.project.models.UsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class CommentService {
    fun addComment(pinId: UUID, authorId: UUID, request: CreateCommentRequest): CommentResponse? = dbQuery {
        val pinExists = PinsTable.selectAll().where { PinsTable.id eq pinId }.empty().not()
        val userExists = UsersTable.selectAll().where { UsersTable.id eq authorId }.empty().not()
        if (!pinExists || !userExists) return@dbQuery null

        val commentId = CommentsTable.insert {
            it[this.pinId] = pinId
            it[this.authorId] = authorId
            it[content] = request.content
        }[CommentsTable.id].value

        CommentsTable.selectAll().where { CommentsTable.id eq commentId }.singleOrNull()?.let(::toComment)
    }

    fun listComments(pinId: UUID): List<CommentResponse> = dbQuery {
        CommentsTable.selectAll()
            .where { CommentsTable.pinId eq pinId }
            .orderBy(CommentsTable.createdAt, SortOrder.ASC)
            .map(::toComment)
    }

    fun deleteComment(commentId: UUID): Boolean = dbQuery {
        CommentsTable.deleteWhere { CommentsTable.id eq commentId } > 0
    }

    private fun toComment(row: ResultRow): CommentResponse = CommentResponse(
        id = row[CommentsTable.id].value.toString(),
        pinId = row[CommentsTable.pinId].value.toString(),
        authorId = row[CommentsTable.authorId].value.toString(),
        content = row[CommentsTable.content],
        createdAt = row[CommentsTable.createdAt].toString(),
    )
}

