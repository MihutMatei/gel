package kronos.project.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String
)

interface MapFacade {
    @Composable
    fun MapView(
        modifier: Modifier,
        markers: List<MapMarker>,
        onMarkerClick: (MapMarker) -> Unit,
        onMapClick: (Double, Double) -> Unit
    )
}
