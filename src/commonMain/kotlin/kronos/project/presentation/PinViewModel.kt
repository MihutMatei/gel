package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kronos.project.Dependencies
import kronos.project.data.remote.dto.PinDto

class PinViewModel : ViewModel() {
    private val pinRepository = Dependencies.pinRepository

    private val _pins = MutableStateFlow<List<PinDto>>(emptyList())
    val pins: StateFlow<List<PinDto>> = _pins.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshPins()
    }

    fun refreshPins() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            pinRepository.fetchPins()
                .onSuccess { _pins.value = it }
                .onFailure { _error.value = it.message ?: "Failed to load pins" }

            _loading.value = false
        }
    }
}

