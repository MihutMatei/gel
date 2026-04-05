package kronos.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kronos.project.map.MapMarker

@Composable
fun MapScreen(
    markers: List<MapMarker>,
    modifier: Modifier = Modifier,
    onMapClick: (Double, Double) -> Unit = { _, _ -> },
) {
    PlatformMapHost(modifier = modifier, markers = markers, onMapClick = onMapClick)
}

@Composable
expect fun PlatformMapHost(
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    onMapClick: (Double, Double) -> Unit = { _, _ -> },
)

@Composable
expect fun rememberLocationPermissionGranted(): Boolean

