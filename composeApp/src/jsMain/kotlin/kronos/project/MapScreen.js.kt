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
import kotlinx.coroutines.delay
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
    val containerRef = remember { ensureMapContainer(mapContainerId, wrapperRef.element) }

    var mapHandle by remember { mutableStateOf<WebMapHandle?>(null) }
    val onMapClickState = rememberUpdatedState(onMapClick)

    Box(modifier = modifier.fillMaxSize()) {}

    LaunchedEffect(Unit) {
        wrapperRef.element.style.setProperty("display", "block", "important")
        wrapperRef.element.style.setProperty("position", "fixed", "important")
        wrapperRef.element.style.setProperty("top", "0", "important")
        wrapperRef.element.style.setProperty("right", "0", "important")
        wrapperRef.element.style.setProperty("bottom", "0", "important")
        wrapperRef.element.style.setProperty("left", "0", "important")
        wrapperRef.element.style.setProperty("width", "100vw", "important")
        wrapperRef.element.style.setProperty("height", "100vh", "important")
        wrapperRef.element.style.setProperty("z-index", "0", "important")

        containerRef.element.style.setProperty("position", "absolute", "important")
        containerRef.element.style.setProperty("top", "0", "important")
        containerRef.element.style.setProperty("right", "0", "important")
        containerRef.element.style.setProperty("bottom", "0", "important")
        containerRef.element.style.setProperty("left", "0", "important")
        containerRef.element.style.setProperty("width", "100%", "important")
        containerRef.element.style.setProperty("height", "100%", "important")

        if (mapHandle == null) {
            // Retry a few times to survive late script/global availability during dev reloads.
            for (attempt in 0 until 20) {
                mapHandle = createBucharestMap(containerRef.element.id)
                if (mapHandle != null) break
                delay(120)
            }
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
        window.setTimeout({
            mapHandle?.resize()
        }, 800)
        window.setTimeout({
            mapHandle?.resize()
        }, 1500)
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
            if (containerRef.createdByCompose) containerRef.element.remove()
            if (wrapperRef.createdByCompose) wrapperRef.element.remove()
        }
    }
}

@Composable
actual fun rememberLocationPermissionGranted(): Boolean = true

private fun ensureMapWrapper(id: String): MapWrapperRef {
    val root = document.getElementById("root") as? HTMLElement
        ?: error("Missing #root")
    val body = document.body ?: error("Missing <body>")

    val existing = document.getElementById(id) as? HTMLElement
    if (existing != null) {
        if (existing.parentElement != body) {
            body.insertBefore(existing, root)
        }
        return MapWrapperRef(existing, createdByCompose = false)
    }

    val wrapper = document.createElement("div") as HTMLElement
    wrapper.id = id
    body.insertBefore(wrapper, root)

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
