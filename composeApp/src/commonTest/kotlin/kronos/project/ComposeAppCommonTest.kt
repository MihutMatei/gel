package kronos.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kronos.project.domain.model.LevelCurve

class ComposeAppCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun levelCurveStartsAtExpectedRanges() {
        assertEquals(1, LevelCurve.levelFromPoints(0))
        assertEquals(1, LevelCurve.levelFromPoints(9))
        assertEquals(2, LevelCurve.levelFromPoints(10))
    }

    @Test
    fun levelCurveGrowsMonotonically() {
        val req2 = LevelCurve.pointsForLevel(2)
        val req50 = LevelCurve.pointsForLevel(50)
        val req100 = LevelCurve.pointsForLevel(100)

        assertTrue(req2 > 0)
        assertTrue(req50 > req2)
        assertTrue(req100 > req50)
    }

    @Test
    fun level99To100CostsAboutTenTimesLevel1To2() {
        val cost1To2 = LevelCurve.pointsForLevel(2) - LevelCurve.pointsForLevel(1)
        val cost99To100 = LevelCurve.pointsForLevel(100) - LevelCurve.pointsForLevel(99)
        assertTrue(cost99To100 >= cost1To2 * 10)
    }
}