package kronos.project.data.remote

object ApiConfig {
    val BASE_URL: String
        get() = resolveBaseUrl()
}

expect fun resolveBaseUrl(): String

