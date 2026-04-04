package kronos.project

import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kronos.project.database.DatabaseFactory
import kronos.project.routes.pinRoutes
import kronos.project.services.CommentService
import kronos.project.services.PinService
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(initDb: Boolean = true) {
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
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }

    val pinService = PinService()
    val commentService = CommentService()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        pinRoutes(pinService, commentService)
    }
}