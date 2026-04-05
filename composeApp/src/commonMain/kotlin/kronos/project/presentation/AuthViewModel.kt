package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kronos.project.Dependencies
import kronos.project.domain.model.AuthState

class AuthViewModel : ViewModel() {
    private val authRepository = Dependencies.authRepository
    private val settingsRepository = Dependencies.settingsRepository
    private val userRepository = Dependencies.userRepository

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

            authRepository.me()
                .onSuccess {
                    _authState.value = AuthState.Authenticated(it)
                    Dependencies.currentUserId.value = it.id
                    syncUserSpecificState()
                }
                .onFailure {
                    authRepository.logout()
                    Dependencies.currentUserId.value = null
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
                    Dependencies.currentUserId.value = it.id
                    syncUserSpecificState()
                }
                .onFailure {
                    Dependencies.currentUserId.value = null
                    _authState.value = AuthState.Unauthenticated
                    _error.value = authRepository.mapErrorMessage(it)
                }
        }
    }

    fun register(username: String, firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            authRepository.register(username, firstName, lastName, email, password)
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
        Dependencies.isDarkMode.value = null
        Dependencies.currentUserId.value = null
        Dependencies.currentUserRole.value = kronos.project.domain.model.UserRole.CITIZEN
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun syncUserSpecificState() {
        settingsRepository.getMySettings().onSuccess {
            Dependencies.isDarkMode.value = it.darkMode
        }
        userRepository.getMyProfile().onSuccess {
            Dependencies.currentUserRole.value = it.role
        }
    }
}

