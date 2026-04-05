package kronos.project

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kronos.project.database.DatabaseFactory
import kronos.project.models.CommentsTable
import kronos.project.models.PinImagesTable
import kronos.project.models.PinsTable
import kronos.project.models.UserSettingsTable
import kronos.project.models.UsersTable
import kronos.project.security.JwtConfig
import org.jetbrains.exposed.sql.SchemaUtils
import java.util.UUID
import kotlin.test.*

class AuthRoutesTest {

    private val testJwtConfig = JwtConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm",
        expiresInHours = 24,
    )

    @Test
    fun registerThenLoginWorks() = authTestApplication {
        val registerBody = """{
          "username":"web_test_user",
          "firstName":"Web",
          "lastName":"Tester",
          "email":"web_test@example.com",
          "password":"password123"
        }""".trimIndent()

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody)
        }
        assertTrue(registerResponse.status.isSuccess(), "Register failed: ${'$'}{registerResponse.status} ${'$'}{registerResponse.bodyAsText()}")

        val loginBody = """{ "email":"web_test@example.com", "password":"password123" }"""
        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginBody)
        }

        assertTrue(loginResponse.status.isSuccess(), "Login failed: ${'$'}{loginResponse.status} ${'$'}{loginResponse.bodyAsText()}")
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

            // minimal jwt config so module(JwtConfig.from(config)) doesn't explode if used
            "jwt.secret" to testJwtConfig.secret,
            "jwt.issuer" to testJwtConfig.issuer,
            "jwt.audience" to testJwtConfig.audience,
            "jwt.realm" to testJwtConfig.realm,
            "jwt.expiresInHours" to testJwtConfig.expiresInHours.toString(),
        )

        DatabaseFactory.init(dbConfig)
        DatabaseFactory.dbQuery {
            SchemaUtils.drop(PinImagesTable, CommentsTable, PinsTable, UserSettingsTable, UsersTable)
            SchemaUtils.create(UsersTable, UserSettingsTable, PinsTable, CommentsTable, PinImagesTable)
        }

        application {
            module(initDb = false, jwtConfigOverride = testJwtConfig)
        }

        testBlock()
    }
}
