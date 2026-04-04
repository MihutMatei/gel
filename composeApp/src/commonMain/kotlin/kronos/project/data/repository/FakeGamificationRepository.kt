package kronos.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kronos.project.domain.model.GamificationState
import kronos.project.domain.repository.GamificationRepository

class FakeGamificationRepository : GamificationRepository {
    private val _state = MutableStateFlow(GamificationState(0, emptyList()))

    override fun getGamificationState(): Flow<GamificationState> = _state.asStateFlow()

    override suspend fun awardPoints(points: Int) {
        _state.value = _state.value.copy(points = _state.value.points + points)
    }

    override suspend fun awardBadge(badge: String) {
        if (!_state.value.badges.contains(badge)) {
            _state.value = _state.value.copy(badges = _state.value.badges + badge)
        }
    }
}
