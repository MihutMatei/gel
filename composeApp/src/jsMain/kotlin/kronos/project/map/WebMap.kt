package kronos.project.map

import kotlinx.browser.window

private const val buildingsLayerId = "gel-3d-buildings"
private const val defaultStyleUrl = "https://api.maptiler.com/maps/dataviz-dark/style.json"

class WebMapHandle(private val map: MapLibreGl.Map) {
    fun destroy() {
        map.remove()
    }
}

fun createBucharestMap(containerId: String): WebMapHandle? {
    val hasMapLibre = js("typeof globalThis.maplibregl !== 'undefined'") as Boolean
    if (!hasMapLibre) {
        window.asDynamic().console.error("MapLibre GL JS is missing. Ensure maplibre-gl.js is loaded in index.html.")
        return null
    }

    val options = js("({})").unsafeCast<MapOptions>()
    options.container = containerId
    options.style = withMapTilerKey(defaultStyleUrl)
    options.center = arrayOf(26.1025, 44.4268)
    options.zoom = 15.4
    options.pitch = 55.0
    options.bearing = -12.0
    options.antialias = true
    options.asDynamic().attributionControl = false

    val map = MapLibreGl.Map(options)
    map.addControl(MapLibreGl.NavigationControl(), "top-right")
    map.on("load") {
        addExtrudedBuildings(map)
    }

    return WebMapHandle(map)
}

private fun withMapTilerKey(styleUrl: String): String {
    val key = (window.asDynamic().MAPTILER_API_KEY as? String)
        ?: (window.asDynamic().MAPTILER_KEY as? String)
        ?: ""

    if (key.isBlank()) return styleUrl
    val separator = if (styleUrl.contains("?")) "&" else "?"
    return "$styleUrl${separator}key=$key"
}

private fun addExtrudedBuildings(map: MapLibreGl.Map) {
    val style = map.getStyle() ?: return
    val sourceName = resolveBuildingSource(style) ?: return

    val layerIds = (style.layers as? Array<dynamic>)?.mapNotNull { it.id as? String }.orEmpty()
    if (buildingsLayerId in layerIds) return

    val paint = js("({})")
    paint["fill-extrusion-color"] = "#A9BBC8"
    paint["fill-extrusion-height"] = arrayOf(
        "coalesce",
        arrayOf("get", "height"),
        arrayOf("get", "render_height"),
        0,
    )
    paint["fill-extrusion-base"] = arrayOf(
        "coalesce",
        arrayOf("get", "min_height"),
        arrayOf("get", "render_min_height"),
        0,
    )
    paint["fill-extrusion-opacity"] = 0.82

    val layer = js("({})").unsafeCast<LayerSpec>()
    layer.id = buildingsLayerId
    layer.type = "fill-extrusion"
    layer.source = sourceName
    layer.minzoom = 14.0
    layer.asDynamic()["source-layer"] = "building"
    layer.paint = paint

    map.addLayer(layer)
}

private fun resolveBuildingSource(style: dynamic): String? {
    val layers = style.layers as? Array<dynamic> ?: return null

    val buildingSource = layers
        .firstOrNull { layer -> layer.asDynamic()["source-layer"] == "building" }
        ?.source as? String

    if (buildingSource != null) return buildingSource

    val sourceKeys = js("Object.keys(style.sources || {})") as Array<String>
    return sourceKeys.firstOrNull()
}

