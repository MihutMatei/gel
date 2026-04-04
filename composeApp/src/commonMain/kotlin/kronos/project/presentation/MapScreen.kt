package kronos.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.map.FakeMapFacade
import kronos.project.map.MapMarker

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
            TopAppBar(
                title = { Text("CivicLens") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateIssue(44.4396, 26.0963) }) {
                Icon(Icons.Default.Add, contentDescription = "Report Issue")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            mapFacade.MapView(
                modifier = Modifier.fillMaxSize(),
                markers = issues.map { 
                    MapMarker(it.id, it.latitude, it.longitude, it.title)
                },
                onMarkerClick = { marker -> onIssueClick(marker.id) },
                onMapClick = { lat, lon -> onCreateIssue(lat, lon) }
            )
            
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Explore local issues",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap a pin to see details or tap the map to report something new.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
