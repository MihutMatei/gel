package kronos.project.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.map.FakeMapFacade
import kronos.project.map.MapMarker

@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f)

    Box(
        modifier = modifier.scale(scale)
    ) {
        if (text != null) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = icon,
                text = text,
                containerColor = containerColor,
                contentColor = contentColor,
                interactionSource = interactionSource
            )
        } else {
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = containerColor,
                contentColor = contentColor,
                interactionSource = interactionSource
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
    viewModel: MapViewModel = viewModel { MapViewModel() }
) {
    val issues by viewModel.issues.collectAsState()
    val mapFacade = remember { FakeMapFacade() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CivicLens", fontWeight = FontWeight.ExtraBold)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedFAB(
                    onClick = { /* My Location Action */ },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    icon = { Icon(Icons.Default.MyLocation, contentDescription = "My Location") }
                )

                AnimatedFAB(
                    onClick = { onCreateIssue(44.4396, 26.0963) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Report Issue") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            mapFacade.MapView(
                modifier = Modifier.fillMaxSize(),
                markers = issues.map {
                    MapMarker(it.id, it.latitude, it.longitude, it.title)
                },
                onMarkerClick = { marker -> onIssueClick(marker.id) },
                onMapClick = { lat, lon -> onCreateIssue(lat, lon) }
            )

            // Search Bar Placeholder
            Card(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding() + 8.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search for issues nearby...", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
