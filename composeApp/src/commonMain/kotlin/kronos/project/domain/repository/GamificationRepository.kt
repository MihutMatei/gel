package kronos.project.domain.repository

import kotlinx.coroutines.flow.Flow
import kronos.project.domain.model.GamificationState

interface GamificationRepository {
    fun getGamificationState(): Flow<GamificationState>
    suspend fun awardPoints(points: Int)
    suspend fun awardBadge(badge: String)
    suspend fun incrementReports(month: String)
}
