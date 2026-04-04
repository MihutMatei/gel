package kronos.project.data.remote

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TokenStorage {
    private val key = "auth.jwt.token"
    private val settings: Settings? = runCatching { Settings() }.getOrNull()
    private val inMemoryFallback = MutableStateFlow<String?>(null)
    private val _tokenFlow = MutableStateFlow(settings?.getStringOrNull(key))

    val tokenFlow: StateFlow<String?> = _tokenFlow

    fun token(): String? = _tokenFlow.value

    fun setToken(token: String) {
        if (settings != null) {
            settings.putString(key, token)
        } else {
            inMemoryFallback.value = token
        }
        _tokenFlow.value = token
    }

    fun clear() {
        if (settings != null) {
            settings.remove(key)
        } else {
            inMemoryFallback.value = null
        }
        _tokenFlow.value = null
    }
}

