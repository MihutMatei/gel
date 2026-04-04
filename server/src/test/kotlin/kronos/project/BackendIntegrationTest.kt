package kronos.project

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kronos.project.database.DatabaseFactory
import kronos.project.dto.AuthUserResponse
import kronos.project.dto.CommentResponse
import kronos.project.dto.LoginResponse
import kronos.project.dto.PinDetailsResponse
import kronos.project.dto.PinStatusDto
import kronos.project.dto.PinSummaryResponse
import kronos.project.security.JwtConfig

class BackendIntegrationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun backendLifecycleWorksWithRealHttpRequests() {
        val dbConfig = MapApplicationConfig(
            "database.initOnStartup" to "true",
            "database.url" to "jdbc:h2:mem:backend_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
            "database.user" to "sa",
            "database.password" to "",
            "database.driver" to "org.h2.Driver",
            "database.maxPoolSize" to "2",
            "jwt.secret" to "test-secret",
            "jwt.issuer" to "test-issuer",
            "jwt.audience" to "test-audience",
            "jwt.realm" to "test-realm",
            "jwt.expiresInHours" to "24",
        )

        DatabaseFactory.init(dbConfig)

        val port = ServerSocket(0).use { it.localPort }
        val server = embeddedServer(Netty, port = port, host = "127.0.0.1") {
            module(
                initDb = false,
                jwtConfigOverride = JwtConfig(
                    secret = "test-secret",
                    issuer = "test-issuer",
                    audience = "test-audience",
                    realm = "test-realm",
                    expiresInHours = 24,
                ),
            )
        }

        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
        val baseUrl = "http://127.0.0.1:$port"

        try {
            server.start(wait = false)
            waitUntilServerIsReady(client, baseUrl)

            val registerBody = """
                {
                  "username": "dummy_user",
                  "email": "dummy-user@example.com",
                  "password": "dummy-password"
                }
            """.trimIndent()
            val registerResponse = request(client, "POST", "$baseUrl/auth/register", registerBody)
            assertEquals(201, registerResponse.statusCode())
            val registeredUser = json.decodeFromString<AuthUserResponse>(registerResponse.body())

            val loginBody = """
                {
                  "email": "dummy-user@example.com",
                  "password": "dummy-password"
                }
            """.trimIndent()
            val loginResponse = request(client, "POST", "$baseUrl/auth/login", loginBody)
            assertEquals(200, loginResponse.statusCode())
            val loginPayload = json.decodeFromString<LoginResponse>(loginResponse.body())
            assertTrue(loginPayload.accessToken.isNotBlank())

            val meResponse = request(
                client,
                "GET",
                "$baseUrl/auth/me",
                headers = mapOf("Authorization" to "Bearer ${loginPayload.accessToken}"),
            )
            assertEquals(200, meResponse.statusCode())
            val meUser = json.decodeFromString<AuthUserResponse>(meResponse.body())
            assertEquals(registeredUser.id, meUser.id)

            val createPinBody = """
                {
                  "title": "Broken streetlight",
                  "description": "Lamp not working near central square",
                  "latitude": 46.7705,
                  "longitude": 23.5899,
                  "category": "lighting",
                  "createdBy": "${registeredUser.id}"
                }
            """.trimIndent()

            val createPinResponse = request(client, "POST", "$baseUrl/pins", createPinBody)
            assertEquals(201, createPinResponse.statusCode())
            val createdPin = json.decodeFromString<PinSummaryResponse>(createPinResponse.body())

            val listPinsResponse = request(client, "GET", "$baseUrl/pins")
            assertEquals(200, listPinsResponse.statusCode())
            val pins = json.decodeFromString<List<PinSummaryResponse>>(listPinsResponse.body())
            assertTrue(pins.any { it.id == createdPin.id })

            val createCommentBody = """
                {
                  "authorId": "${registeredUser.id}",
                  "content": "I can confirm this issue."
                }
            """.trimIndent()

            val createCommentResponse = request(client, "POST", "$baseUrl/pins/${createdPin.id}/comments", createCommentBody)
            assertEquals(201, createCommentResponse.statusCode())
            val createdComment = json.decodeFromString<CommentResponse>(createCommentResponse.body())

            val detailsResponse = request(client, "GET", "$baseUrl/pins/${createdPin.id}")
            assertEquals(200, detailsResponse.statusCode())
            val details = json.decodeFromString<PinDetailsResponse>(detailsResponse.body())
            assertEquals(createdPin.id, details.pin.id)
            assertEquals(1, details.comments.size)

            val commentsResponse = request(client, "GET", "$baseUrl/pins/${createdPin.id}/comments")
            assertEquals(200, commentsResponse.statusCode())
            val comments = json.decodeFromString<List<CommentResponse>>(commentsResponse.body())
            assertEquals(1, comments.size)
            assertEquals(createdComment.id, comments.first().id)

            val patchResponse = request(
                client,
                "PATCH",
                "$baseUrl/pins/${createdPin.id}",
                """{"status":"resolved"}""",
            )
            assertEquals(200, patchResponse.statusCode())
            val updatedPin = json.decodeFromString<PinSummaryResponse>(patchResponse.body())
            assertEquals(PinStatusDto.RESOLVED, updatedPin.status)

            val deleteCommentResponse = request(client, "DELETE", "$baseUrl/comments/${createdComment.id}")
            assertEquals(204, deleteCommentResponse.statusCode())

            val deletePinResponse = request(client, "DELETE", "$baseUrl/pins/${createdPin.id}")
            assertEquals(204, deletePinResponse.statusCode())

            val getDeletedPinResponse = request(client, "GET", "$baseUrl/pins/${createdPin.id}")
            assertEquals(404, getDeletedPinResponse.statusCode())
        } finally {
            server.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        }
    }

    private fun request(
        client: HttpClient,
        method: String,
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse<String> {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/json")

        headers.forEach { (name, value) ->
            builder.header(name, value)
        }

        if (body != null) {
            builder.header("Content-Type", "application/json")
            builder.method(method, HttpRequest.BodyPublishers.ofString(body))
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody())
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun waitUntilServerIsReady(client: HttpClient, baseUrl: String) {
        repeat(20) {
            val response = runCatching { request(client, "GET", "$baseUrl/") }.getOrNull()
            if (response?.statusCode() == 200) return
            Thread.sleep(100)
        }
        error("Server did not start in time")
    }
}

