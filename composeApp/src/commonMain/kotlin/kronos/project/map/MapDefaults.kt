package kronos.project.map

object MapDefaults {
    const val styleUrl = "https://api.maptiler.com/maps/dataviz-dark/style.json"

    const val centerLongitude = 26.1025
    const val centerLatitude = 44.4268
    const val zoom = 15.4
    const val pitch = 55.0
    const val bearing = -12.0

    const val buildingsLayerId = "gel-3d-buildings"
    const val buildingSourceLayer = "building"
    const val buildingsMinZoom = 14.0

    const val buildingsColor = "#A9BBC8"
    const val buildingsOpacity = 0.82
}

data class WebMapConfig(
    val containerId: String,
    val mapTilerKey: String = "",
    val styleUrl: String = MapDefaults.styleUrl,
    val centerLongitude: Double = MapDefaults.centerLongitude,
    val centerLatitude: Double = MapDefaults.centerLatitude,
    val zoom: Double = MapDefaults.zoom,
    val pitch: Double = MapDefaults.pitch,
    val bearing: Double = MapDefaults.bearing,
)

data class IssuePin(
    val id: String,
    val title: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
)

data class MapPoint(
    val latitude: Double,
    val longitude: Double,
)

data class WebMapCallbacks(
    val onMapTapped: (MapPoint) -> Unit = {},
    val onIssueSelected: (IssuePin) -> Unit = {},
)

