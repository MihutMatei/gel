package kronos.project.data.remote

actual fun resolveBaseUrl(): String {
    val buildConfigOverride = runCatching { kronos.project.BuildConfig.BACKEND_BASE_URL }.getOrDefault("")
    val override = buildConfigOverride.trim()
    return override.ifBlank { "http://10.0.2.2:8080" }
}

