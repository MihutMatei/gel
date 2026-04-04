package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kronos.project.Dependencies

class MapViewModel : ViewModel() {
    val issues = Dependencies.getIssues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
