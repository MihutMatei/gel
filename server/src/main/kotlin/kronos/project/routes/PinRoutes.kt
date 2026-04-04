package kronos.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kronos.project.dto.CreateCommentRequest
import kronos.project.dto.CreatePinRequest
import kronos.project.dto.ErrorResponse
import kronos.project.dto.UpdatePinRequest
import kronos.project.dto.VoteCommentRequest
import kronos.project.models.PinStatus
import kronos.project.services.CommentService
import kronos.project.services.PinFilters
import kronos.project.services.PinService
import java.util.UUID

fun Route.pinRoutes(pinService: PinService, commentService: CommentService) {
    route("/pins") {
        get {
            val statusParam = call.request.queryParameters["status"]
            val status = statusParam?.let {
                parsePinStatus(it) ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid status value: $it"),
                )
            }

            val latMinRaw = call.request.queryParameters["lat_min"]
            val latMin = latMinRaw?.toDoubleOrNull()
            if (latMinRaw != null && latMin == null) {
                return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("lat_min must be a valid number"))
            }

            val latMaxRaw = call.request.queryParameters["lat_max"]
            val latMax = latMaxRaw?.toDoubleOrNull()
            if (latMaxRaw != null && latMax == null) {
                return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("lat_max must be a valid number"))
            }

            val lonMinRaw = call.request.queryParameters["lon_min"]
            val lonMin = lonMinRaw?.toDoubleOrNull()
            if (lonMinRaw != null && lonMin == null) {
                return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("lon_min must be a valid number"))
            }

            val lonMaxRaw = call.request.queryParameters["lon_max"]
            val lonMax = lonMaxRaw?.toDoubleOrNull()
            if (lonMaxRaw != null && lonMax == null) {
                return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("lon_max must be a valid number"))
            }

            val pins = pinService.listPins(
                PinFilters(
                    category = call.request.queryParameters["category"],
                    status = status,
                    latMin = latMin,
                    latMax = latMax,
                    lonMin = lonMin,
                    lonMax = lonMax,
                ),
            )
            call.respond(HttpStatusCode.OK, pins)
        }

        post {
            val request = runCatching { call.receive<CreatePinRequest>() }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }

            if (!request.isValid()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin payload"))
            }

            val createdBy = request.createdBy.toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("createdBy must be a valid UUID"))

            val created = pinService.createPin(request, createdBy)
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Cannot create pin: user not found"),
                )

            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}") {
            val id = call.parameters["id"].toUuidOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin id"))

            val details = pinService.getPinDetails(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Pin not found"))

            call.respond(HttpStatusCode.OK, details)
        }

        patch("/{id}") {
            val id = call.parameters["id"].toUuidOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin id"))

            val request = runCatching { call.receive<UpdatePinRequest>() }
                .getOrElse {
                    return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }

            if (request.description == null && request.status == null) {
                return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("At least one field (description/status) is required"),
                )
            }

            val updated = pinService.updatePin(id, request)
                ?: return@patch call.respond(HttpStatusCode.NotFound, ErrorResponse("Pin not found"))

            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"].toUuidOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin id"))

            val deleted = pinService.deletePin(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Pin not found"))
            }

            call.respond(HttpStatusCode.NoContent)
        }

        post("/{id}/comments") {
            val pinId = call.parameters["id"].toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin id"))

            val request = runCatching { call.receive<CreateCommentRequest>() }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }

            if (request.content.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Comment content cannot be blank"))
            }

            val authorId = request.authorId.toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("authorId must be a valid UUID"))

            val created = commentService.addComment(pinId, authorId, request)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Pin or author not found"))

            call.respond(HttpStatusCode.Created, created)
        }

        get("/{id}/comments") {
            val pinId = call.parameters["id"].toUuidOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid pin id"))

            val pinExists = pinService.getPinDetails(pinId) != null
            if (!pinExists) {
                return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Pin not found"))
            }

            call.respond(HttpStatusCode.OK, commentService.listComments(pinId))
        }
    }

    route("/comments") {
        delete("/{id}") {
            val commentId = call.parameters["id"].toUuidOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid comment id"))

            val deleted = commentService.deleteComment(commentId)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Comment not found"))
            }

            call.respond(HttpStatusCode.NoContent)
        }

        post("/{id}/reply") {
            val parentId = call.parameters["id"].toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid comment id"))

            val request = runCatching { call.receive<CreateCommentRequest>() }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }

            if (request.content.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Reply content cannot be blank"))
            }

            val authorId = request.authorId.toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("authorId must be a valid UUID"))

            val parentComment = commentService.getCommentById(parentId)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Parent comment not found"))

            val pinId = parentComment.pinId.toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Invalid pin reference"))

            val reply = commentService.addComment(
                pinId = pinId,
                authorId = authorId,
                request = request.copy(parentId = parentId.toString()),
            ) ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Author not found"))

            call.respond(HttpStatusCode.Created, reply)
        }

        post("/{id}/vote") {
            val commentId = call.parameters["id"].toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid comment id"))

            val request = runCatching { call.receive<VoteCommentRequest>() }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }

            val userId = request.userId.toUuidOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId must be a valid UUID"))

            val success = commentService.voteOnComment(commentId, userId, request.isUpvote)
            if (!success) {
                return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Comment or user not found"))
            }

            call.respond(HttpStatusCode.OK, mapOf("success" to true))
        }
    }
}

private fun String?.toUuidOrNull(): UUID? = runCatching {
    this?.let(UUID::fromString)
}.getOrNull()

private fun parsePinStatus(raw: String): PinStatus? = when (raw.lowercase()) {
    "open" -> PinStatus.OPEN
    "in_progress" -> PinStatus.IN_PROGRESS
    "resolved" -> PinStatus.RESOLVED
    else -> null
}


private fun CreatePinRequest.isValid(): Boolean {
    if (title.isBlank() || description.isBlank() || category.isBlank()) return false
    if (latitude !in -90.0..90.0) return false
    if (longitude !in -180.0..180.0) return false
    return true
}

