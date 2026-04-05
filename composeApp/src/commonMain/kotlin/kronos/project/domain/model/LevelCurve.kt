package kronos.project.domain.model

import kotlin.math.ceil
import kotlin.math.pow

object LevelCurve {
    const val MAX_LEVEL = 100
    private const val BASE_LEVEL_STEP = 10.0
    // Chosen so level 99->100 takes about 10x the points of level 1->2.
    private val growth = 10.0.pow(1.0 / (MAX_LEVEL - 2))

    fun pointsForLevel(level: Int): Int {
        if (level <= 1) return 0
        val clamped = level.coerceAtMost(MAX_LEVEL)
        val exponent = (clamped - 1).toDouble()
        val total = BASE_LEVEL_STEP * ((growth.pow(exponent) - 1.0) / (growth - 1.0))
        return ceil(total).toInt()
    }

    fun levelFromPoints(points: Int): Int {
        if (points <= 0) return 1
        var level = 1
        while (level < MAX_LEVEL && points >= pointsForLevel(level + 1)) {
            level++
        }
        return level
    }

    fun progressToNextLevel(points: Int): Float {
        val level = levelFromPoints(points)
        if (level >= MAX_LEVEL) return 1f

        val currentStart = pointsForLevel(level)
        val nextStart = pointsForLevel(level + 1)
        val span = (nextStart - currentStart).coerceAtLeast(1)
        val inLevel = (points - currentStart).coerceIn(0, span)
        return inLevel.toFloat() / span.toFloat()
    }
}

