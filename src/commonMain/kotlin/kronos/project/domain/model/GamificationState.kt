package kronos.project.domain.model

data class GamificationState(
    val points: Int,
    val level: Int,
    val totalReports: Int,
    val badges: List<String>,
    val monthlyHistory: Map<String, Int> = emptyMap()
)
