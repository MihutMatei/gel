package kronos.project

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kronos.project.security.JwtConfig
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
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
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }
}