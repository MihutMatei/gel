package kronos.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.getElementById("root") as? HTMLElement
        ?: error("Missing #root in index.html")

    ComposeViewport(root) {
        App()
    }
}