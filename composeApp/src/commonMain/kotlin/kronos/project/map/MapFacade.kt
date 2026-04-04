package kronos.project.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MapMarkerCard(
    val title: String,
    val subtitle: String? = null,
    val body: String? = null,
)

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val card: MapMarkerCard? = null,
)

interface MapFacade {
    @Composable
    fun MapView(
        modifier: Modifier,
        markers: List<MapMarker>,
        onMarkerClick: (MapMarker) -> Unit,
        onMapClick: (Double, Double) -> Unit,
    )
}
