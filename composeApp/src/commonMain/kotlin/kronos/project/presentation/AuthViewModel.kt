package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kronos.project.Dependencies
import kronos.project.domain.model.AuthState

class AuthViewModel : ViewModel() {
    private val authRepository = Dependencies.authRepository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            val result = withTimeoutOrNull(5000) {
                authRepository.me()
            }

            if (result == null) {
                // Timeout
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            result.onSuccess {
                _authState.value = AuthState.Authenticated(it)
                Dependencies.currentUser.value = it
            }
            .onFailure {
                authRepository.logout()
                Dependencies.currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            authRepository.login(email, password)
                .onSuccess {
                    _authState.value = AuthState.Authenticated(it)
                    Dependencies.currentUser.value = it
                }
                .onFailure {
                    _authState.value = AuthState.Unauthenticated
                    _error.value = authRepository.mapErrorMessage(it)
                }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            authRepository.register(username, email, password)
                .onSuccess {
                    login(email = email, password = password)
                }
                .onFailure {
                    _error.value = authRepository.mapErrorMessage(it)
                }
        }
    }

    fun logout() {
        authRepository.logout()
        Dependencies.currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        _error.value = null
    }
}

