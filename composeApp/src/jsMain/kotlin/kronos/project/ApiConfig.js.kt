package kronos.project.data.remote

import kotlinx.browser.window

actual fun resolveBaseUrl(): String {
    val host = window.location.hostname.ifBlank { "localhost" }
    val protocol = if (window.location.protocol.startsWith("https")) "https" else "http"
    return "$protocol://$host:8080"
}

