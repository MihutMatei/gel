package kronos.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kronos.project.dto.ErrorResponse
import kronos.project.dto.SettingsDto
import kronos.project.dto.UpdateUserProfileRequest
import kronos.project.services.UserService
import java.util.UUID

fun Route.userRoutes(userService: UserService) {
    authenticate("auth-jwt") {
        route("/users/me") {
            get("/settings") {
                val userId = call.userIdFromJwt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
                call.respond(HttpStatusCode.OK, userService.getSettings(userId))
            }

            put("/settings") {
                val userId = call.userIdFromJwt()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
                val request = runCatching { call.receive<SettingsDto>() }.getOrElse {
                    return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }
                if (request.language.isBlank()) {
                    return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("language cannot be blank"))
                }
                call.respond(HttpStatusCode.OK, userService.putSettings(userId, request))
            }

            get("/profile") {
                val userId = call.userIdFromJwt()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
                val profile = userService.getProfile(userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                call.respond(HttpStatusCode.OK, profile)
            }

            put("/profile") {
                val userId = call.userIdFromJwt()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
                val request = runCatching { call.receive<UpdateUserProfileRequest>() }.getOrElse {
                    return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
                }
                if (request.firstName.isBlank() || request.lastName.isBlank() || request.role.isBlank()) {
                    return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("firstName, lastName and role are required"))
                }
                val updated = userService.updateProfile(userId, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                call.respond(HttpStatusCode.OK, updated)
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.userIdFromJwt(): UUID? {
    val principal = principal<JWTPrincipal>() ?: return null
    return principal.subject?.let { runCatching { UUID.fromString(it) }.getOrNull() }
}

