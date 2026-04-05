package kronos.project.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gel.composeapp.generated.resources.Res
import gel.composeapp.generated.resources.app_name
import kronos.project.MapScreen as PlatformMapScreen
import kronos.project.map.MapDefaults
import kronos.project.map.MapMarker
import kronos.project.map.MapMarkerCard
import kronos.project.map.MapThreadPost
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private val demoMarkerTemplates = listOf(
    MapMarker(
        id = "demo-cluster-1",
        latitude = MapDefaults.centerLatitude,
        longitude = MapDefaults.centerLongitude,
        title = "Streetlight not working on Calea Victoriei",
        category = "Utilities",
        card = MapMarkerCard(
            title = "Streetlight not working on Calea Victoriei",
            mainPost = MapThreadPost(
                author = "matei_urbanwatch",
                content = "Street lighting is off around the roundabout after 22:00. Visibility is poor and pedestrians are hard to see.",
                upvotes = 42,
                downvotes = 3,
            ),
            comments = listOf(
                MapThreadPost("ana_reports", "I can confirm this. It has been dark for at least 3 nights.", 11, 0),
                MapThreadPost("ion_commuter", "Cars speed there. This should be fixed urgently.", 7, 1),
                MapThreadPost("city_observer", "Maybe a blown fuse in that segment. Reported it to city support too.", 5, 0),
            ),
        ),
    ),
    MapMarker(
        id = "demo-cluster-2",
        latitude = MapDefaults.centerLatitude + 0.00022,
        longitude = MapDefaults.centerLongitude - 0.00018,
        title = "Pothole growing near tram line",
        category = "Public transport",
        card = MapMarkerCard(
            title = "Pothole growing near tram line",
            mainPost = MapThreadPost(
                author = "roadwatch_ro",
                content = "Large pothole near the tram rail. Cars are swerving into the next lane to avoid it.",
                upvotes = 29,
                downvotes = 2,
            ),
            comments = listOf(
                MapThreadPost("bus_driver_17", "Hit this with the bus yesterday. Needs quick repair.", 15, 0),
                MapThreadPost("civic_eye", "Added photos in municipal app too.", 6, 0),
            ),
        ),
    ),
    MapMarker(
        id = "demo-cluster-3",
        latitude = MapDefaults.centerLatitude - 0.00019,
        longitude = MapDefaults.centerLongitude + 0.00024,
        title = "Crosswalk paint faded",
        category = "Crime / safety",
        card = MapMarkerCard(
            title = "Crosswalk paint faded",
            mainPost = MapThreadPost(
                author = "safe_walks",
                content = "The zebra crossing is barely visible at night and in rain.",
                upvotes = 18,
                downvotes = 1,
            ),
            comments = listOf(
                MapThreadPost("school_parent", "Kids use this crossing every morning.", 12, 0),
                MapThreadPost("urban_planner88", "Should be repainted with reflective paint.", 8, 0),
            ),
        ),
    ),
    MapMarker(
        id = "demo-spread-1",
        latitude = MapDefaults.centerLatitude + 0.0023,
        longitude = MapDefaults.centerLongitude - 0.0019,
        title = "Overflowing trash bins",
        category = "Utilities",
        card = MapMarkerCard(
            title = "Overflowing trash bins",
            mainPost = MapThreadPost(
                author = "green_block",
                content = "Bins have not been collected for days. Trash spills onto the sidewalk.",
                upvotes = 24,
                downvotes = 4,
            ),
            comments = listOf(
                MapThreadPost("neighbor21", "Strong smell in the afternoon.", 9, 0),
                MapThreadPost("clean_city_now", "Collection truck skipped this street twice.", 10, 1),
            ),
        ),
    ),
    MapMarker(
        id = "demo-spread-2",
        latitude = MapDefaults.centerLatitude - 0.0027,
        longitude = MapDefaults.centerLongitude + 0.0021,
        title = "Broken traffic signal timing",
        category = "Public transport",
        card = MapMarkerCard(
            title = "Broken traffic signal timing",
            mainPost = MapThreadPost(
                author = "commute_daily",
                content = "Pedestrian light stays green for only a few seconds. Elderly people cannot cross in time.",
                upvotes = 37,
                downvotes = 2,
            ),
            comments = listOf(
                MapThreadPost("night_runner", "I saw people stranded in the middle lane.", 13, 0),
                MapThreadPost("traffic_nerd", "Cycle appears unsynced with adjacent intersection.", 7, 0),
                MapThreadPost("volunteer_ro", "Submitted formal complaint this morning.", 5, 0),
            ),
        ),
    ),
    MapMarker(
        id = "demo-spread-3",
        latitude = MapDefaults.centerLatitude + 0.0031,
        longitude = MapDefaults.centerLongitude + 0.0014,
        title = "Graffiti and vandalized bus shelter",
        category = "Crime / safety",
        card = MapMarkerCard(
            title = "Graffiti and vandalized bus shelter",
            mainPost = MapThreadPost(
                author = "district_watch",
                content = "Glass panel is cracked and bench is damaged. Shelter is unsafe during crowded hours.",
                upvotes = 16,
                downvotes = 5,
            ),
            comments = listOf(
                MapThreadPost("daily_bus_user", "People avoid waiting inside now.", 8, 1),
                MapThreadPost("fixit_team", "Can coordinate with transit operator.", 4, 0),
            ),
        ),
    ),
)

private const val BUCHAREST_MIN_LAT = 44.33
private const val BUCHAREST_MAX_LAT = 44.54
private const val BUCHAREST_MIN_LON = 25.95
private const val BUCHAREST_MAX_LON = 26.25
private const val MAX_DEMO_MARKERS = 100
private const val DEMO_MIN_DISTANCE_METERS = 120.0
private const val DEMO_CANDIDATE_ATTEMPTS = 20_000
private const val DEMO_SPAWN_RADIUS_METERS = 4_500.0
private const val DEMO_PIN_RANDOM_SEED = 20260405

private val demoMarkers = generateBucharestDemoMarkers(demoMarkerTemplates)

private data class DemoPoint(
    val latitude: Double,
    val longitude: Double,
)

private fun generateBucharestDemoMarkers(templates: List<MapMarker>): List<MapMarker> {
    if (templates.isEmpty()) return emptyList()

    val random = Random(DEMO_PIN_RANDOM_SEED)
    val acceptedPoints = mutableListOf<DemoPoint>()
    var attempts = 0

    while (acceptedPoints.size < MAX_DEMO_MARKERS && attempts < DEMO_CANDIDATE_ATTEMPTS) {
        val candidate = randomDemoPoint(random)
        val canPlace = acceptedPoints.all { existing ->
            distanceMeters(existing, candidate) >= DEMO_MIN_DISTANCE_METERS
        }
        if (canPlace) {
            acceptedPoints += candidate
        }
        attempts++
    }

    while (acceptedPoints.size < MAX_DEMO_MARKERS) {
        acceptedPoints += randomDemoPoint(random)
    }

    val templateOffset = random.nextInt(templates.size)
    return acceptedPoints.mapIndexed { index, point ->
        val template = templates[(templateOffset + index) % templates.size]
        template.copy(
            id = "demo-distributed-$index-${template.id}",
            latitude = point.latitude,
            longitude = point.longitude,
        )
    }
}

private fun randomDemoPoint(random: Random): DemoPoint {
    val angle = random.nextDouble(0.0, PI * 2.0)
    val radius = sqrt(random.nextDouble()) * DEMO_SPAWN_RADIUS_METERS
    val lonMetersAtCenter = 111_320.0 * cos(MapDefaults.centerLatitude * PI / 180.0).coerceAtLeast(0.2)

    val latOffset = (radius * cos(angle)) / 111_320.0
    val lonOffset = (radius * sin(angle)) / lonMetersAtCenter

    return DemoPoint(
        latitude = (MapDefaults.centerLatitude + latOffset).coerceIn(BUCHAREST_MIN_LAT, BUCHAREST_MAX_LAT),
        longitude = (MapDefaults.centerLongitude + lonOffset).coerceIn(BUCHAREST_MIN_LON, BUCHAREST_MAX_LON),
    )
}

private fun distanceMeters(a: DemoPoint, b: DemoPoint): Double {
    val avgLatRadians = ((a.latitude + b.latitude) * 0.5) * PI / 180.0
    val dLatMeters = (a.latitude - b.latitude) * 111_320.0
    val dLonMeters = (a.longitude - b.longitude) * 111_320.0 * cos(avgLatRadians).coerceAtLeast(0.2)
    return sqrt((dLatMeters * dLatMeters) + (dLonMeters * dLonMeters))
}

@Composable
private fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "fab_scale")

    Box(modifier = modifier.scale(scale)) {
        if (text != null) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = icon,
                text = text,
                interactionSource = interactionSource,
            )
        } else {
            SmallFloatingActionButton(
                onClick = onClick,
                interactionSource = interactionSource,
                elevation = FloatingActionButtonDefaults.elevation(),
            ) {
                icon()
            }
        }
    }
}

@Composable
fun MapScreen(
    onIssueClick: (String) -> Unit,
    onCreateIssue: (Double, Double) -> Unit,
    onProfileClick: () -> Unit,
    pinViewModel: PinViewModel = viewModel { PinViewModel() },
) {
    val pins by pinViewModel.pins.collectAsState()

    val liveMarkers = pins.map { pin ->
        MapMarker(
            id = pin.id,
            latitude = pin.latitude,
            longitude = pin.longitude,
            title = pin.title,
            category = pin.category,
            card = MapMarkerCard(
                title = pin.title,
                mainPost = MapThreadPost(
                    author = "reporter",
                    content = pin.description,
                ),
            ),
        )
    }

    val markers = (demoMarkers + liveMarkers).distinctBy { it.id }

    Box(modifier = Modifier.fillMaxSize()) {
        PlatformMapScreen(
            modifier = Modifier.fillMaxSize(),
            markers = markers,
            onMapClick = { lat, long ->
                onCreateIssue(lat, long)
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Lens,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.app_name),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            IconButton(onClick = onProfileClick) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFB0B7C3)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.padding(6.dp),
                        tint = Color(0xFF4B5563)
                    )
                }
            }
        }

        AnimatedFAB(
            onClick = { onCreateIssue(44.4396, 26.0963) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add request") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}