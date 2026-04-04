package kronos.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kronos.project.map.MapMarker

@Composable
fun MapScreen(
    markers: List<MapMarker>,
    modifier: Modifier = Modifier,
) {
    PlatformMapHost(modifier = modifier, markers = markers)
}

@Composable
expect fun PlatformMapHost(modifier: Modifier = Modifier, markers: List<MapMarker> = emptyList())

@Composable
expect fun rememberLocationPermissionGranted(): Boolean

