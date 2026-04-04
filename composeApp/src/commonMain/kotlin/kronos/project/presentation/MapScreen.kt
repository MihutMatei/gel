package kronos.project.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import kronos.project.MapScreen as PlatformMapScreen
import kronos.project.map.MapMarker
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
private fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f)

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

    val markers = pins.map { pin ->
        MapMarker(
            id = pin.id,
            latitude = pin.latitude,
            longitude = pin.longitude,
            title = pin.title,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lens, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CivicLens", fontWeight = FontWeight.ExtraBold)
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
                AnimatedFAB(
                    onClick = { onCreateIssue(44.4396, 26.0963) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Report Issue") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PlatformMapScreen(
                modifier = Modifier.fillMaxSize(),
                markers = markers,
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
                        "Latest: ${pins.first().title}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            Card(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding() + 8.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search for issues nearby...", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
