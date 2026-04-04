package kronos.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kronos.project.domain.model.GamificationState
import kronos.project.domain.repository.GamificationRepository

class FakeGamificationRepository : GamificationRepository {
    private val _state = MutableStateFlow(
        GamificationState(
            points = 450,
            level = 4,
            totalReports = 12,
            badges = listOf("First Report", "Community Helper"),
            monthlyHistory = mapOf(
                "Jan" to 2,
                "Feb" to 5,
                "Mar" to 8,
                "Apr" to 4,
                "May" to 10,
                "Jun" to 12
            )
        )
    )

    override fun getGamificationState(): Flow<GamificationState> = _state.asStateFlow()

    override suspend fun awardPoints(points: Int) {
        val newPoints = _state.value.points + points
        val newLevel = (newPoints / 100) + 1
        _state.value = _state.value.copy(points = newPoints, level = newLevel)
        checkBadges()
    }

    override suspend fun awardBadge(badge: String) {
        if (!_state.value.badges.contains(badge)) {
            _state.value = _state.value.copy(badges = _state.value.badges + badge)
        }
    }

    override suspend fun incrementReports(month: String) {
        val currentHistory = _state.value.monthlyHistory.toMutableMap()
        currentHistory[month] = (currentHistory[month] ?: 0) + 1
        _state.value = _state.value.copy(
            totalReports = _state.value.totalReports + 1,
            monthlyHistory = currentHistory
        )
        checkBadges()
    }

    private fun checkBadges() {
        val current = _state.value
        val newBadges = current.badges.toMutableSet()
        
        if (current.totalReports >= 1 && !newBadges.contains("First Report")) newBadges.add("First Report")
        if (current.totalReports >= 10 && !newBadges.contains("Active Citizen")) newBadges.add("Active Citizen")
        if (current.points >= 500 && !newBadges.contains("Bronze Hero")) newBadges.add("Bronze Hero")
        if (current.points >= 1000 && !newBadges.contains("Silver Hero")) newBadges.add("Silver Hero")
        
        if (newBadges.size > current.badges.size) {
            _state.value = current.copy(badges = newBadges.toList())
        }
    }
}
