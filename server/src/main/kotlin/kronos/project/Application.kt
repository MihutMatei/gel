package kronos.project

import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kronos.project.database.DatabaseFactory
import kronos.project.dto.ErrorResponse
import kronos.project.routes.authRoutes
import kronos.project.routes.pinRoutes
import kronos.project.routes.userRoutes
import kronos.project.security.JwtConfig
import kronos.project.services.AuthService
import kronos.project.services.CommentService
import kronos.project.services.PinService
import kronos.project.services.UserService
import kotlinx.serialization.json.Json

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module(initDb: Boolean = true, jwtConfigOverride: JwtConfig? = null) {
    val jwtConfig = jwtConfigOverride ?: JwtConfig.from(environment.config)

    if (initDb) {
        DatabaseFactory.init(environment.config)
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier())
            validate { credential ->
                if (credential.payload.subject.isNullOrBlank()) {
                    null
                } else {
                    JWTPrincipal(credential.payload)
                }
            }
            challenge { _, _ ->
                call.respond(io.ktor.http.HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
            }
        }
    }

    val authService = AuthService()
    val pinService = PinService()
    val commentService = CommentService()
    val userService = UserService()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        authRoutes(authService, jwtConfig)
        userRoutes(userService)
        pinRoutes(pinService, commentService)
    }
}