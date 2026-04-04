package kronos.project

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertNotNull

class ClockTest {
    @Test
    fun testClock() {
        try {
            println("[DEBUG_LOG] Attempting to load Clock.System")
            val clockClass = Class.forName("kotlinx.datetime.Clock\$System")
            println("[DEBUG_LOG] Successfully loaded Clock.System: $clockClass")
        } catch (e: Throwable) {
            println("[DEBUG_LOG] Failed to load Clock.System: $e")
            e.printStackTrace()
        }
        
        try {
            println("[DEBUG_LOG] Attempting to load Clock")
            val clockClass = Class.forName("kotlinx.datetime.Clock")
            println("[DEBUG_LOG] Successfully loaded Clock: $clockClass")
        } catch (e: Throwable) {
            println("[DEBUG_LOG] Failed to load Clock: $e")
            e.printStackTrace()
        }

        println("[DEBUG_LOG] Clock.System.now() = ${Clock.System.now()}")
        assertNotNull(Clock.System.now())
    }
}
