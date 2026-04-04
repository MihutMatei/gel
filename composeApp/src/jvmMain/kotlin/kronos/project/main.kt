package kronos.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.datetime.Clock

fun main() {
    println("[DEBUG_LOG] Clock.System.now() = ${Clock.System.now()}")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CivicLens",
        ) {
            App()
        }
    }
}