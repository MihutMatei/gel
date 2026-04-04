@file:Suppress("unused")

package kronos.project.map

data class WebMapConfig(
    val containerId: String,
    val mapTilerKey: String = "",
    val styleUrl: String = "",
    val centerLongitude: Double = 26.1025,
    val centerLatitude: Double = 44.4268,
    val zoom: Double = 15.4,
    val pitch: Double = 55.0,
    val bearing: Double = -12.0,
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

private var legacyHandle: WebMapHandle? = null

@Suppress("UNUSED_PARAMETER")
fun initializeWebMap(
    config: WebMapConfig,
    initialPins: List<IssuePin>,
    callbacks: WebMapCallbacks = WebMapCallbacks(),
) {
    legacyHandle?.destroy()
    legacyHandle = createBucharestMap(config.containerId)
}

@Suppress("UNUSED_PARAMETER")
fun setIssuePins(pins: List<IssuePin>) {
}

@Suppress("UNUSED_PARAMETER")
fun setSelectedIssuePin(pinId: String?) {
}
