package kronos.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kronos.project.dto.ErrorResponse
import kronos.project.dto.LoginRequest
import kronos.project.dto.LoginResponse
import kronos.project.dto.RegisterRequest
import kronos.project.security.JwtConfig
import kronos.project.services.AuthService
import kronos.project.services.RegisterResult
import java.util.UUID

private val usernameRegex = Regex("^[A-Za-z0-9_]{3,30}$")
private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

fun Route.authRoutes(authService: AuthService, jwtConfig: JwtConfig) {
    route("/auth") {
        post("/register") {
            val request = runCatching { call.receive<RegisterRequest>() }.getOrElse {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
            }

            validateRegisterRequest(request)?.let { message ->
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message))
            }

            when (val result = authService.register(request)) {
                is RegisterResult.Success -> call.respond(HttpStatusCode.Created, result.user)
                RegisterResult.UsernameTaken -> call.respond(HttpStatusCode.Conflict, ErrorResponse("Username is already taken"))
                RegisterResult.EmailTaken -> call.respond(HttpStatusCode.Conflict, ErrorResponse("Email is already taken"))
                RegisterResult.Failed -> call.respond(HttpStatusCode.BadRequest, ErrorResponse("Registration failed"))
            }
        }

        post("/login") {
            val request = runCatching { call.receive<LoginRequest>() }.getOrElse {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
            }

            validateLoginRequest(request)?.let { message ->
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message))
            }

            val user = authService.login(request)
            if (user == null) {
                return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
            }

            val token = runCatching { jwtConfig.generateToken(UUID.fromString(user.id), user.username) }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                }

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(
                    accessToken = token,
                    expiresInSeconds = jwtConfig.expiresInSeconds,
                    user = user,
                ),
            )
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))

                val userId = principal.subject?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))

                val user = authService.findById(userId)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))

                call.respond(HttpStatusCode.OK, user)
            }
        }
    }
}

private fun validateRegisterRequest(request: RegisterRequest): String? {
    if (!usernameRegex.matches(request.username)) {
        return "username must be 3-30 characters and contain only letters, numbers, and underscores"
    }
    if (!emailRegex.matches(request.email)) {
        return "email format is invalid"
    }
    if (request.password.length < 8) {
        return "password must have at least 8 characters"
    }
    return null
}

private fun validateLoginRequest(request: LoginRequest): String? {
    if (!emailRegex.matches(request.email)) {
        return "email format is invalid"
    }
    if (request.password.length < 8) {
        return "password must have at least 8 characters"
    }
    return null
}

