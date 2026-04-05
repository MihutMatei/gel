package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kronos.project.Dependencies
import kronos.project.domain.model.SettingsDto
import kronos.project.domain.model.UserProfile

class SettingsViewModel : ViewModel() {
    private val settingsRepository = Dependencies.settingsRepository
    private val userRepository = Dependencies.userRepository

    private val _settings = MutableStateFlow<SettingsDto?>(null)
    val settings: StateFlow<SettingsDto?> = _settings.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            settingsRepository.getMySettings()
                .onSuccess {
                    _settings.value = it
                    Dependencies.isDarkMode.value = it.darkMode
                }
                .onFailure { _error.value = it.message ?: "Failed to load settings" }

            userRepository.getMyProfile()
                .onSuccess {
                    _profile.value = it
                    Dependencies.currentUserRole.value = it.role
                }
                .onFailure { _error.value = it.message ?: "Failed to load profile" }
        }
    }

    fun save(settings: SettingsDto, profile: UserProfile) {
        viewModelScope.launch {
            settingsRepository.putMySettings(settings)
                .onSuccess {
                    _settings.value = it
                    Dependencies.isDarkMode.value = it.darkMode
                    _error.value = null
                }
                .onFailure { _error.value = it.message ?: "Failed to save settings" }

            userRepository.putMyProfile(profile)
                .onSuccess {
                    _profile.value = it
                    Dependencies.currentUserRole.value = it.role
                    _error.value = null
                }
                .onFailure { _error.value = it.message ?: "Failed to save profile" }
        }
    }
}

