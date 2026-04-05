package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kronos.project.Dependencies
import kronos.project.domain.model.LevelCurve
import kronos.project.domain.model.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val gamificationState = combine(
        Dependencies.gamificationRepository.getGamificationState(),
        _userProfile,
    ) { state, profile ->
        if (profile == null) {
            state
        } else {
            state.copy(
                points = profile.points,
                level = LevelCurve.levelFromPoints(profile.points),
                totalReports = profile.reports,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUserRole = combine(Dependencies.currentUserRole, _userProfile) { fallback, profile ->
        profile?.role ?: fallback
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Dependencies.currentUserRole.value)

    init {
        viewModelScope.launch {
            Dependencies.userRepository.getMyProfile().onSuccess {
                _userProfile.value = it
                Dependencies.currentUserRole.value = it.role
            }
        }
    }

    fun switchRole() = Unit
}
