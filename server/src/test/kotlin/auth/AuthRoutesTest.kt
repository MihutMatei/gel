package auth

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kronos.project.database.DatabaseFactory
import kronos.project.dto.AuthUserResponse
import kronos.project.dto.LoginResponse
import kronos.project.models.CommentsTable
import kronos.project.models.PinImagesTable
import kronos.project.models.PinsTable
import kronos.project.models.UsersTable
import kronos.project.module
import kronos.project.security.JwtConfig
import org.jetbrains.exposed.sql.SchemaUtils

class AuthRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val testJwtConfig = JwtConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm",
        expiresInHours = 24,
    )

    @Test
    fun registerSuccessReturns201AndUserWithoutPasswordHash() = authTestApplication {
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "username": "alice_user",
                  "email": "alice@example.com",
                  "password": "strongpass123"
                }
                """.trimIndent(),
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        val user = json.decodeFromString<AuthUserResponse>(body)
        assertEquals("alice_user", user.username)
        assertEquals("alice@example.com", user.email)
        assertFalse(body.contains("passwordHash"))
        assertFalse(body.contains("password_hash"))
    }

    @Test
    fun duplicateUsernameOrEmailReturns409() = authTestApplication {
        register("alice_user", "alice@example.com", "strongpass123")

        val duplicateUsername = register("alice_user", "alice2@example.com", "strongpass123")
        assertEquals(HttpStatusCode.Conflict, duplicateUsername.status)

        val duplicateEmail = register("alice_user_2", "alice@example.com", "strongpass123")
        assertEquals(HttpStatusCode.Conflict, duplicateEmail.status)
    }

    @Test
    fun invalidRegistrationInputReturns400() = authTestApplication {
        val badEmail = register("alice_user", "not-an-email", "strongpass123")
        assertEquals(HttpStatusCode.BadRequest, badEmail.status)

        val shortPassword = register("bob_user", "bob@example.com", "short")
        assertEquals(HttpStatusCode.BadRequest, shortPassword.status)
    }

    @Test
    fun loginSuccessReturns200WithJwt() = authTestApplication {
        register("alice_user", "alice@example.com", "strongpass123")

        val response = login("alice@example.com", "strongpass123")
        assertEquals(HttpStatusCode.OK, response.status)

        val login = json.decodeFromString<LoginResponse>(response.bodyAsText())
        assertTrue(login.accessToken.isNotBlank())
        assertEquals("Bearer", login.tokenType)
        assertEquals("alice_user", login.user.username)
    }

    @Test
    fun wrongCredentialsReturns401() = authTestApplication {
        register("alice_user", "alice@example.com", "strongpass123")

        val response = login("alice@example.com", "wrongpassword")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun meWithValidTokenReturns200() = authTestApplication {
        register("alice_user", "alice@example.com", "strongpass123")
        val login = json.decodeFromString<LoginResponse>(login("alice@example.com", "strongpass123").bodyAsText())

        val response = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer ${login.accessToken}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val me = json.decodeFromString<AuthUserResponse>(response.bodyAsText())
        assertEquals(login.user.id, me.id)
        assertEquals("alice_user", me.username)
    }

    @Test
    fun meWithMissingOrInvalidTokenReturns401() = authTestApplication {
        val missingToken = client.get("/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, missingToken.status)

        val invalidToken = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer definitely-not-a-jwt")
        }
        assertEquals(HttpStatusCode.Unauthorized, invalidToken.status)
    }

    private suspend fun ApplicationTestBuilder.register(username: String, email: String, password: String) =
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "username": "$username",
                  "email": "$email",
                  "password": "$password"
                }
                """.trimIndent(),
            )
        }

    private suspend fun ApplicationTestBuilder.login(email: String, password: String) =
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "email": "$email",
                  "password": "$password"
                }
                """.trimIndent(),
            )
        }

    private fun authTestApplication(testBlock: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        val dbName = "auth_test_${UUID.randomUUID().toString().replace("-", "")}"
        val dbConfig = MapApplicationConfig(
            "database.initOnStartup" to "true",
            "database.url" to "jdbc:h2:mem:$dbName;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
            "database.user" to "sa",
            "database.password" to "",
            "database.driver" to "org.h2.Driver",
            "database.maxPoolSize" to "2",
        )

        DatabaseFactory.init(dbConfig)
        DatabaseFactory.dbQuery {
            SchemaUtils.drop(PinImagesTable, CommentsTable, PinsTable, UsersTable)
            SchemaUtils.create(UsersTable, PinsTable, CommentsTable, PinImagesTable)
        }

        application {
            module(initDb = false, jwtConfigOverride = testJwtConfig)
        }

        testBlock()
    }
}

