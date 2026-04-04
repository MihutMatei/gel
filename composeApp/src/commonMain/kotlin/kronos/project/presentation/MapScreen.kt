package kronos.project.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.MapScreen as PlatformMapScreen
import kronos.project.map.MapDefaults
import kronos.project.map.MapMarker
import kronos.project.map.MapMarkerCard
import kronos.project.map.MapThreadPost

private val demoMarkers = listOf(
    MapMarker(
        id = "demo-cluster-1",
        latitude = MapDefaults.centerLatitude,
        longitude = MapDefaults.centerLongitude,
        title = "Streetlight not working on Calea Victoriei",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onIssueClick: (String) -> Unit,
    onCreateIssue: (Double, Double) -> Unit,
    onProfileClick: () -> Unit,
    pinViewModel: PinViewModel = viewModel { PinViewModel() },
) {
    val pins by pinViewModel.pins.collectAsState()
    val loading by pinViewModel.loading.collectAsState()
    val error by pinViewModel.error.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                pinViewModel.refreshPins()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val liveMarkers = pins.map { pin ->
        MapMarker(
            id = pin.id,
            latitude = pin.latitude,
            longitude = pin.longitude,
            title = pin.title,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Search for issues nearby...", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Surface(shape = RoundedCornerShape(20.dp)) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.padding(4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedFAB(
                    onClick = { pinViewModel.refreshPins() },
                    icon = { Icon(Icons.Default.MyLocation, contentDescription = "Refresh pins") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PlatformMapScreen(
                modifier = Modifier.fillMaxSize(),
                markers = markers,
                onMapClick = { lat, long ->
                    onCreateIssue(lat, long)
                }
            )

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error ?: "Failed to fetch pins",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = padding.calculateTopPadding() + 64.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                )
            }

            if (pins.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 90.dp)
                        .clickable { onIssueClick(pins.first().id) },
                ) {
                    Text(
                        text = "Latest: ${pins.first().title}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}