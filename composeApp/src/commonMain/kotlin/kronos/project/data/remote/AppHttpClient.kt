package kronos.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object AppHttpClient {
    fun create(tokenStorage: TokenStorage): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            val token = tokenStorage.token()
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}

