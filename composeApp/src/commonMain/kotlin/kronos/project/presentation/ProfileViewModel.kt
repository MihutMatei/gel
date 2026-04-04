package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kronos.project.Dependencies
import kronos.project.domain.model.UserRole

class ProfileViewModel : ViewModel() {
    val gamificationState = Dependencies.gamificationRepository.getGamificationState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUserRole = Dependencies.currentUserRole

    fun switchRole() {
        Dependencies.currentUserRole.value = if (Dependencies.currentUserRole.value == UserRole.CITIZEN) {
            UserRole.TOWNHALL_EMPLOYEE
        } else {
            UserRole.CITIZEN
        }
    }
}
