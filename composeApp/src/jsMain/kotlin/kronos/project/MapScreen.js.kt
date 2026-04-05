package kronos.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kronos.project.map.MapMarker
import kronos.project.map.WebMapHandle
import kronos.project.map.createBucharestMap
import org.w3c.dom.HTMLElement

private const val mapContainerId = "gel-map"

@Composable
actual fun PlatformMapHost(modifier: Modifier, markers: List<MapMarker>) {
    val containerState = remember { mutableStateOf<MapContainerRef?>(null) }
    val mapState = remember { mutableStateOf<WebMapHandle?>(null) }

    Box(modifier = modifier.fillMaxSize())

    DisposableEffect(Unit) {
        val containerRef = ensureMapContainer(mapContainerId)
        containerState.value = containerRef

        if (mapState.value == null) {
            val mapHandle = runCatching { createBucharestMap(containerRef.element.id) }
                .onFailure { error ->
                    println("Failed to initialize web map: ${error.message}")
                    if (containerRef.createdByCompose) {
                        containerRef.element.remove()
                    }
                    containerState.value = null
                }
                .getOrNull()
            mapState.value = mapHandle
        }

        onDispose {
            mapState.value?.destroy()
            mapState.value = null
            if (containerRef.createdByCompose) {
                containerRef.element.remove()
            }
            containerState.value = null
        }
    }
}

@Composable
actual fun rememberLocationPermissionGranted(): Boolean = true

private data class MapContainerRef(
    val element: HTMLElement,
    val createdByCompose: Boolean,
)

private fun ensureMapContainer(id: String): MapContainerRef {
    val existing = document.getElementById(id) as? HTMLElement
    if (existing != null) {
        return MapContainerRef(existing, createdByCompose = false)
    }

    val container = document.createElement("div") as HTMLElement
    container.id = id
    container.style.position = "fixed"
    container.style.top = "0"
    container.style.right = "0"
    container.style.bottom = "0"
    container.style.left = "0"
    // Keep the map underneath Compose content to avoid covering the app UI.
    container.style.zIndex = "-1"

    val root = document.getElementById("root")
    val body = document.body
    if (root != null && body != null) {
        body.insertBefore(container, root)
    } else {
        body?.appendChild(container)
    }
    return MapContainerRef(container, createdByCompose = true)
}
