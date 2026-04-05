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
import kotlinx.browser.window
import kronos.project.map.MapMarker
import kronos.project.map.WebMapHandle
import kronos.project.map.createBucharestMap
import org.w3c.dom.HTMLElement

private const val mapContainerId = "gel-map"
private const val mapWrapperId = "gel-map-wrapper"

private data class MapContainerRef(
    val element: HTMLElement,
    val createdByCompose: Boolean,
)

private data class MapWrapperRef(
    val element: HTMLElement,
    val createdByCompose: Boolean,
)

@Composable
actual fun PlatformMapHost(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMapClick: (Double, Double) -> Unit,
) {
    val wrapperRef = remember { ensureMapWrapper(mapWrapperId) }
    val containerRef: MapContainerRef = remember { ensureMapContainer(mapContainerId, wrapperRef.element) }

    var mapHandle by remember { mutableStateOf<WebMapHandle?>(null) }
    val onMapClickState = rememberUpdatedState(onMapClick)

    Box(modifier = modifier.fillMaxSize()) {}

    LaunchedEffect(Unit) {
        wrapperRef.element.style.position = "fixed"
        wrapperRef.element.style.top = "0"
        wrapperRef.element.style.right = "0"
        wrapperRef.element.style.bottom = "0"
        wrapperRef.element.style.left = "0"
        wrapperRef.element.style.width = "100vw"
        wrapperRef.element.style.height = "100vh"
        wrapperRef.element.style.zIndex = "0"

        containerRef.element.style.position = "absolute"
        containerRef.element.style.top = "0"
        containerRef.element.style.right = "0"
        containerRef.element.style.bottom = "0"
        containerRef.element.style.left = "0"
        containerRef.element.style.width = "100%"
        containerRef.element.style.height = "100%"

        mapHandle = createBucharestMap(containerRef.element.id)

        window.setTimeout({
            mapHandle?.asDynamic()?.map?.resize?.call(mapHandle.asDynamic().map)
        }, 50)
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
            if (containerRef.createdByCompose) containerRef.element.remove()
            if (wrapperRef.createdByCompose) wrapperRef.element.remove()
        }
    }
}

@Composable
actual fun rememberLocationPermissionGranted(): Boolean = true

private fun ensureMapWrapper(id: String): MapWrapperRef {
    val host = document.getElementById("root") as? HTMLElement
        ?: error("Missing #root")

    val existing = document.getElementById(id) as? HTMLElement
    if (existing != null) {
        return MapWrapperRef(existing, createdByCompose = false)
    }

    val wrapper = document.createElement("div") as HTMLElement
    wrapper.id = id

    // Important: pune harta în spatele UI-ului Compose, nu peste el
    if (host.firstChild != null) {
        host.insertBefore(wrapper, host.firstChild)
    } else {
        host.appendChild(wrapper)
    }

    return MapWrapperRef(wrapper, createdByCompose = true)
}

private fun ensureMapContainer(id: String, parent: HTMLElement): MapContainerRef {
    val existing = document.getElementById(id) as? HTMLElement
    if (existing != null) {
        if (existing.parentElement != parent) {
            parent.appendChild(existing)
        }
        return MapContainerRef(existing, createdByCompose = false)
    }

    val container = document.createElement("div") as HTMLElement
    container.id = id
    parent.appendChild(container)
    return MapContainerRef(container, createdByCompose = true)
}