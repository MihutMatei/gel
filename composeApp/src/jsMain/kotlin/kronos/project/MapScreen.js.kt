package kronos.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
            mapState.value = createBucharestMap(containerRef.element.id)
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

    LaunchedEffect(mapState.value, markers) {
        mapState.value?.setMarkers(markers)
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
    document.body?.appendChild(container)
    return MapContainerRef(container, createdByCompose = true)
}
