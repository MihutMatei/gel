package kronos.project.domain.usecase

import kronos.project.domain.repository.GamificationRepository

class AwardPointsForIssue(private val repository: GamificationRepository) {
    suspend operator fun invoke(points: Int) = repository.awardPoints(points)
}
