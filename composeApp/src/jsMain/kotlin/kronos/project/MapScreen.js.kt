package kronos.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.browser.window
import kronos.project.map.MapMarker
import kronos.project.map.WebMapHandle
import kronos.project.map.createBucharestMap
import org.w3c.dom.HTMLElement

private const val mapHostId = "map-host"
private const val mapContainerId = "gel-map"

@Composable
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val host = remember {
        document.getElementById(mapHostId) as? HTMLElement
            ?: error("Missing #$mapHostId in index.html")
    }
    val container = remember {
        document.getElementById(mapContainerId) as? HTMLElement
            ?: error("Missing #$mapContainerId in index.html")
    }

    var mapHandle by remember { mutableStateOf<WebMapHandle?>(null) }
    val onMapClickState = rememberUpdatedState(onMapClick)

    Box(modifier = modifier.fillMaxSize()) {}

    LaunchedEffect(Unit) {
        host.style.display = "block"
        host.style.position = "fixed"
        host.style.top = "0"
        host.style.right = "0"
        host.style.bottom = "0"
        host.style.left = "0"

        container.style.position = "absolute"
        container.style.top = "0"
        container.style.right = "0"
        container.style.bottom = "0"
        container.style.left = "0"
        container.style.width = "100%"
        container.style.height = "100%"

        if (mapHandle == null) {
            mapHandle = createBucharestMap(container.id)
        }

        window.requestAnimationFrame {
            mapHandle?.resize()
        }
        window.setTimeout({
            mapHandle?.resize()
        }, 50)
        window.setTimeout({
            mapHandle?.resize()
        }, 200)
    }

    LaunchedEffect(mapHandle, markers) {
        mapHandle?.setMarkers(markers)
    }

    LaunchedEffect(mapHandle) {
        mapHandle?.setMapClickHandler { lat, lng ->
            onMapClickState.value(lat, lng)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapHandle?.destroy()
            mapHandle = null
            host.style.display = "none"
        }
    }
}

@Composable
actual fun rememberLocationPermissionGranted(): Boolean = true