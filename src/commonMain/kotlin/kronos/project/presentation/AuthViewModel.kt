package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

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
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    Dependencies.currentUserId.value = user.id

                    launch {
                        runCatching {
                            withTimeout(5_000) { syncUserSpecificState() }
                        }.onFailure {
                            println("[AuthViewModel] refreshSession sync failed: ${'$'}it")
                        }
                    }
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
            _isSubmitting.value = true
            _error.value = null
            println("[AuthViewModel] login start email=$email")
            try {
                authRepository.login(email, password)
                    .onSuccess { user ->
                        println("[AuthViewModel] login success")
                        _authState.value = AuthState.Authenticated(user)
                        Dependencies.currentUserId.value = user.id

                        // Don't block UI / navigation on these; also avoid hanging forever.
                        launch {
                            runCatching {
                                withTimeout(5_000) {
                                    syncUserSpecificState()
                                }
                            }.onFailure {
                                println("[AuthViewModel] syncUserSpecificState failed: ${'$'}it")
                            }
                        }
                    }
                    .onFailure {
                        println("[AuthViewModel] login failure: ${'$'}{it.message}")
                        Dependencies.currentUserId.value = null
                        _authState.value = AuthState.Unauthenticated
                        _error.value = authRepository.mapErrorMessage(it)
                    }
            } catch (t: Throwable) {
                println("[AuthViewModel] login exception: ${'$'}t")
                _error.value = authRepository.mapErrorMessage(t)
            } finally {
                println("[AuthViewModel] login end -> submitting=false")
                _isSubmitting.value = false
            }
        }
    }

    fun register(username: String, firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            try {
                authRepository.register(username, firstName, lastName, email, password)
                    .onSuccess {
                        // Run the login call inline so submitting stays accurate.
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
                    .onFailure {
                        _error.value = authRepository.mapErrorMessage(it)
                    }
            } finally {
                _isSubmitting.value = false
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
        // Avoid throwing (especially important for JS): keep it best-effort.
        settingsRepository.getMySettings().onSuccess {
            Dependencies.isDarkMode.value = it.darkMode
        }
        userRepository.getMyProfile().onSuccess {
            Dependencies.currentUserRole.value = it.role
        }
    }
}
