package kronos.project.map

import kotlinx.browser.window

class WebMapHandle(private val map: MapLibreGl.Map) {
    private val activeMarkers = mutableListOf<dynamic>()

    fun setMarkers(markers: List<MapMarker>) {
        clearMarkers()

        val mapLibre = js("globalThis.maplibregl")
        markers.forEach { marker ->
            val popup = mapLibre.Popup(js("({ offset: 20 })"))
            val cardHtml = marker.card?.toPopupHtml()
            if (cardHtml.isNullOrBlank()) {
                popup.setText(marker.title.ifBlank { "Reported issue" })
            } else {
                popup.setHTML(cardHtml)
            }

            val markerView = mapLibre.Marker(js("({ color: '#FF3D00' })"))
                .setLngLat(arrayOf(marker.longitude, marker.latitude))
                .setPopup(popup)
                .addTo(map)

            activeMarkers.add(markerView)
        }
    }

    fun destroy() {
        clearMarkers()
        map.remove()
    }

    private fun clearMarkers() {
        activeMarkers.forEach { it.remove() }
        activeMarkers.clear()
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
    options.style = withMapTilerKey(MapDefaults.styleUrl)
    options.center = arrayOf(MapDefaults.centerLongitude, MapDefaults.centerLatitude)
    options.zoom = MapDefaults.zoom
    options.pitch = MapDefaults.pitch
    options.bearing = MapDefaults.bearing
    options.antialias = true
    options.asDynamic().attributionControl = false

    val map = MapLibreGl.Map(options)
    map.addControl(
        MapLibreGl.NavigationControl(
            js("({ showZoom: false, showCompass: false })"),
        ),
        "top-right",
    )
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
    if (MapDefaults.buildingsLayerId in layerIds) return

    val paint = js("({})")
    paint["fill-extrusion-color"] = MapDefaults.buildingsColor
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
    paint["fill-extrusion-opacity"] = MapDefaults.buildingsOpacity

    val layer = js("({})").unsafeCast<LayerSpec>()
    layer.id = MapDefaults.buildingsLayerId
    layer.type = "fill-extrusion"
    layer.source = sourceName
    layer.minzoom = MapDefaults.buildingsMinZoom
    layer.asDynamic()["source-layer"] = MapDefaults.buildingSourceLayer
    layer.paint = paint

    map.addLayer(layer)
}

private fun resolveBuildingSource(style: dynamic): String? {
    val layers = style.layers as? Array<dynamic> ?: return null

    val buildingSource = layers
        .firstOrNull { layer -> layer.asDynamic()["source-layer"] == MapDefaults.buildingSourceLayer }
        ?.source as? String

    if (buildingSource != null) return buildingSource

    val sourceKeys = js("Object.keys(style.sources || {})") as Array<String>
    return sourceKeys.firstOrNull()
}

private fun MapMarkerCard.toPopupHtml(): String {
    val titleHtml = title.escapeHtml()
    val subtitleHtml = subtitle?.takeIf { it.isNotBlank() }?.escapeHtml()
    val bodyHtml = body?.takeIf { it.isNotBlank() }?.escapeHtml()?.replace("\n", "<br/>")

    val subtitleBlock = if (subtitleHtml == null) "" else "<div style='margin-top:4px;color:#5f6368;font-size:12px;'>$subtitleHtml</div>"
    val bodyBlock = if (bodyHtml == null) "" else "<div style='margin-top:8px;color:#202124;font-size:12px;line-height:1.4;'>$bodyHtml</div>"

    return (
        "<div style='min-width:220px;max-width:280px;padding:2px 4px;'>"
            + "<div style='font-weight:600;font-size:14px;color:#202124;'>$titleHtml</div>"
            + subtitleBlock
            + bodyBlock
            + "</div>"
        )
}

private fun String.escapeHtml(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")
