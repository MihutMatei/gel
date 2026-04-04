package kronos.project.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MapThreadPost(
    val author: String,
    val content: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
)

data class MapMarkerCard(
    val title: String,
    val mainPost: MapThreadPost,
    val comments: List<MapThreadPost> = emptyList(),
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
