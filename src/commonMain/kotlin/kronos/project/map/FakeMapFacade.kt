package kronos.project.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class FakeMapFacade : MapFacade {
    @Composable
    override fun MapView(
        modifier: Modifier,
        markers: List<MapMarker>,
        onMarkerClick: (MapMarker) -> Unit,
        onMapClick: (Double, Double) -> Unit
    ) {
        Box(
            modifier = modifier
                .background(Color(0xFFE0E0E0)) // Light gray for map background
                .clickable { onMapClick(44.4396, 26.0963) } // Simulate click in center
        ) {
            Text(
                text = "Fake 2.5D Map View (Placeholder)\nTap anywhere to simulate creating a pin at center",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray,
                fontSize = 14.sp
            )

            markers.forEach { marker ->
                // Basic representation of markers on a fake 2D plane
                // In a real map these would be at lat/long projected to screen coords
                val xOffset = (marker.longitude - 26.10) * 1000
                val yOffset = (marker.latitude - 44.43) * 1000
                
                Box(
                    modifier = Modifier
                        .offset(x = xOffset.dp, y = yOffset.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onMarkerClick(marker) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", color = Color.White)
                }
            }
        }
    }
}
