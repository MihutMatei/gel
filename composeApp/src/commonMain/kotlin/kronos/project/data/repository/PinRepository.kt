package kronos.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kronos.project.data.remote.ApiConfig
import kronos.project.data.remote.dto.PinDto

class PinRepository(private val httpClient: HttpClient) {
    suspend fun fetchPins(): Result<List<PinDto>> = runCatching {
        httpClient.get("${ApiConfig.BASE_URL}/pins").body<List<PinDto>>()
    }
}

