package kronos.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kronos.project.map.MapMarker
import kronos.project.map.WebMapHandle
import kronos.project.map.createBucharestMap
import org.w3c.dom.HTMLElement

private const val mapContainerId = "gel-map"

@Composable
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val containerRef = remember { ensureMapContainer(mapContainerId) }
    var mapHandle by remember { mutableStateOf<WebMapHandle?>(null) }
    val onMapClickState = rememberUpdatedState(onMapClick)

    Box(modifier = modifier.fillMaxSize()) {}

    LaunchedEffect(Unit) {
        mapHandle = createBucharestMap(containerRef.element.id)
    }

    LaunchedEffect(mapHandle, markers) {
        mapHandle?.setMarkers(markers)
    }

    LaunchedEffect(mapHandle) {
        mapHandle?.setMapClickHandler { lat, lng -> onMapClickState.value(lat, lng) }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapHandle?.destroy()
            mapHandle = null
            if (containerRef.createdByCompose) {
                containerRef.element.remove()
            }
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
    document.body?.appendChild(container)
    return MapContainerRef(container, createdByCompose = true)
}
