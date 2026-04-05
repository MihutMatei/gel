@file:Suppress("unused")

package kronos.project.map

external interface MapOptions {
    var container: String
    var style: dynamic
    var center: Array<Double>
    var zoom: Double
    var pitch: Double
    var bearing: Double
    var antialias: Boolean?
}

external interface LayerSpec {
    var id: String
    var type: String
    var source: String
    var minzoom: Double?
    var paint: dynamic
}

@JsName("maplibregl")
external object MapLibreGl {
    class Map(options: MapOptions) {
        fun addControl(control: NavigationControl, position: String = definedExternally)
        fun on(event: String, listener: (dynamic) -> Unit)
        fun addLayer(layer: LayerSpec, beforeId: String = definedExternally)
        fun addSource(id: String, source: dynamic)
        fun setStyle(style: dynamic)
        fun remove()
        fun getStyle(): dynamic
        fun resize()
    }

    class NavigationControl(options: dynamic = definedExternally)
}

