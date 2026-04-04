package kronos.project.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expiresInHours: Long,
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun verifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: UUID, username: String): String {
        val expiresAt = Date.from(Instant.now().plus(expiresInHours, ChronoUnit.HOURS))
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("username", username)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    val expiresInSeconds: Long get() = expiresInHours * 3600

    companion object {
        fun from(config: ApplicationConfig): JwtConfig = JwtConfig(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            realm = config.propertyOrNull("jwt.realm")?.getString() ?: "Access to city reporter API",
            expiresInHours = config.propertyOrNull("jwt.expiresInHours")?.getString()?.toLongOrNull() ?: 24,
        )
    }
}

