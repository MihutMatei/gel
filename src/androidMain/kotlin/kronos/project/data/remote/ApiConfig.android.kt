package kronos.project.data.remote

import android.os.Build

actual object ApiConfig {
    /**
     * Android networking:
     * - Emulator -> use 10.0.2.2 to reach your host machine's localhost
     * - Physical device -> must use your computer's LAN IP (or an override)
     */
    actual val BASE_URL: String
        get() = ApiConfigOverride.baseUrlOverride
            ?: if (isProbablyEmulator()) "http://10.0.2.2:8080" else "http://127.0.0.1:8080"

    private fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val brand = Build.BRAND
        val device = Build.DEVICE
        val manufacturer = Build.MANUFACTURER
        val product = Build.PRODUCT

        return fingerprint.contains("generic", ignoreCase = true) ||
            fingerprint.contains("unknown", ignoreCase = true) ||
            model.contains("google_sdk", ignoreCase = true) ||
            model.contains("Emulator", ignoreCase = true) ||
            model.contains("Android SDK built for", ignoreCase = true) ||
            manufacturer.contains("Genymotion", ignoreCase = true) ||
            (brand.startsWith("generic", ignoreCase = true) && device.startsWith("generic", ignoreCase = true)) ||
            product.contains("sdk", ignoreCase = true)
    }
}
