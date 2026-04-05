package kronos.project.data.remote

/**
 * Optional runtime override for BASE_URL on Android.
 *
 * If you're on a physical device, `127.0.0.1` won't reach your computer.
 * Call this once at app start with something like `http://192.168.x.x:8080`.
 */
object ApiConfigOverride {
    @Volatile
    var baseUrlOverride: String? = null
        private set

    fun setBaseUrl(url: String) {
        baseUrlOverride = url.trim().trimEnd('/')
    }
}
