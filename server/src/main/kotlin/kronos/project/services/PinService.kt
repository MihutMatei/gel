package kronos.project.services

import kronos.project.database.DatabaseFactory.dbQuery
import kronos.project.dto.CommentResponse
import kronos.project.dto.CreatePinRequest
import kronos.project.dto.PinDetailsResponse
import kronos.project.dto.PinImageResponse
import kronos.project.dto.PinSummaryResponse
import kronos.project.dto.UpdatePinRequest
import kronos.project.dto.toDto
import kronos.project.dto.toModel
import kronos.project.models.CommentsTable
import kronos.project.models.PinImagesTable
import kronos.project.models.PinStatus
import kronos.project.models.PinsTable
import kronos.project.models.UsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

data class PinFilters(
    val category: String?,
    val status: PinStatus?,
    val latMin: Double?,
    val latMax: Double?,
    val lonMin: Double?,
    val lonMax: Double?,
)

class PinService {
    fun listPins(filters: PinFilters): List<PinSummaryResponse> = dbQuery {
        val query = PinsTable.selectAll()

        filters.category?.let { query.andWhere { PinsTable.category eq it } }
        filters.status?.let { query.andWhere { PinsTable.status eq it } }
        filters.latMin?.let { query.andWhere { PinsTable.latitude greaterEq it } }
        filters.latMax?.let { query.andWhere { PinsTable.latitude lessEq it } }
        filters.lonMin?.let { query.andWhere { PinsTable.longitude greaterEq it } }
        filters.lonMax?.let { query.andWhere { PinsTable.longitude lessEq it } }

        query.orderBy(PinsTable.createdAt, SortOrder.DESC).map(::toPinSummary)
    }

    fun createPin(request: CreatePinRequest, createdBy: UUID): PinSummaryResponse? = dbQuery {
        val userExists = UsersTable.selectAll().where { UsersTable.id eq createdBy }.empty().not()
        if (!userExists) return@dbQuery null

        val pinId = PinsTable.insert {
            it[title] = request.title
            it[description] = request.description
            it[latitude] = request.latitude
            it[longitude] = request.longitude
            it[category] = request.category
            it[status] = PinStatus.OPEN
            it[this.createdBy] = createdBy
        }[PinsTable.id].value

        PinsTable.selectAll().where { PinsTable.id eq pinId }.singleOrNull()?.let(::toPinSummary)
    }

    fun getPinDetails(pinId: UUID): PinDetailsResponse? = dbQuery {
        val pin = PinsTable.selectAll().where { PinsTable.id eq pinId }.singleOrNull()?.let(::toPinSummary)
            ?: return@dbQuery null

        val comments = CommentsTable.selectAll()
            .where { CommentsTable.pinId eq pinId }
            .orderBy(CommentsTable.createdAt, SortOrder.ASC)
            .map(::toComment)

        val images = PinImagesTable.selectAll()
            .where { PinImagesTable.pinId eq pinId }
            .orderBy(PinImagesTable.uploadedAt, SortOrder.ASC)
            .map(::toImage)

        PinDetailsResponse(pin = pin, comments = comments, images = images)
    }

    fun updatePin(pinId: UUID, request: UpdatePinRequest): PinSummaryResponse? = dbQuery {
        val updatedRows = PinsTable.update({ PinsTable.id eq pinId }) {
            request.description?.let { description -> it[PinsTable.description] = description }
            request.status?.let { status -> it[PinsTable.status] = status.toModel() }
        }

        if (updatedRows == 0) return@dbQuery null
        PinsTable.selectAll().where { PinsTable.id eq pinId }.singleOrNull()?.let(::toPinSummary)
    }

    fun deletePin(pinId: UUID): Boolean = dbQuery {
        PinsTable.deleteWhere { PinsTable.id eq pinId } > 0
    }

    private fun toPinSummary(row: ResultRow): PinSummaryResponse = PinSummaryResponse(
        id = row[PinsTable.id].value.toString(),
        title = row[PinsTable.title],
        description = row[PinsTable.description],
        latitude = row[PinsTable.latitude],
        longitude = row[PinsTable.longitude],
        category = row[PinsTable.category],
        status = row[PinsTable.status].toDto(),
        createdBy = row[PinsTable.createdBy].value.toString(),
        createdAt = row[PinsTable.createdAt].toString(),
    )

    private fun toComment(row: ResultRow): CommentResponse = CommentResponse(
        id = row[CommentsTable.id].value.toString(),
        pinId = row[CommentsTable.pinId].value.toString(),
        parentId = row[CommentsTable.parentId]?.value?.toString(),
        authorId = row[CommentsTable.authorId].value.toString(),
        content = row[CommentsTable.content],
        createdAt = row[CommentsTable.createdAt].toString(),
        upvotes = row[CommentsTable.upvotes],
        downvotes = row[CommentsTable.downvotes],
    )

    private fun toImage(row: ResultRow): PinImageResponse = PinImageResponse(
        id = row[PinImagesTable.id].value.toString(),
        pinId = row[PinImagesTable.pinId].value.toString(),
        imageUrl = row[PinImagesTable.imageUrl],
        uploadedAt = row[PinImagesTable.uploadedAt].toString(),
    )
}

