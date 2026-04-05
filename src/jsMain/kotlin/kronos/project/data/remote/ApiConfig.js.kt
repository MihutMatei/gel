package kronos.project.data.remote

import kotlinx.browser.window

actual object ApiConfig {
    /**
     * Web default:
     * - UI (dev server) is on 8081
     * - Backend (Ktor) is on 8080
     *
     * Override by setting `window.__API_BASE_URL__` in `index.html`.
     */
    actual val BASE_URL: String
        get() {
            // runtime-config.js sets window.__API_BASE_URL__
            val override = window.asDynamic().__API_BASE_URL__ as? String
            if (!override.isNullOrBlank()) return override.trim().trimEnd('/')

            val override2 = window.asDynamic().__API_BASE_URL as? String
            if (!override2.isNullOrBlank()) return override2.trim().trimEnd('/')

            // Default to local Ktor server.
            return "http://localhost:8080"
        }
}
