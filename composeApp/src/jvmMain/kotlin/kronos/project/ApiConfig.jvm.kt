package kronos.project.data.remote

actual fun resolveBaseUrl(): String {
    val propertyOverride = System.getProperty("backend.baseUrl")?.trim().orEmpty()
    if (propertyOverride.isNotBlank()) return propertyOverride

    val envOverride = System.getenv("BACKEND_BASE_URL")?.trim().orEmpty()
    return envOverride.ifBlank { "http://127.0.0.1:8080" }
}

