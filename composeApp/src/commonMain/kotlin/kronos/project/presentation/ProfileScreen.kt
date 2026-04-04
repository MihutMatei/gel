package kronos.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Role: $currentUserRole", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.switchRole() }) {
                        Text("Switch Role")
                    }
                }
            }

            gamificationState?.let { state ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gamification", style = MaterialTheme.typography.titleLarge)
                        Text("Points: ${state.points}", style = MaterialTheme.typography.displaySmall)
                        
                        if (state.badges.isNotEmpty()) {
                            Text("Badges:", style = MaterialTheme.typography.titleMedium)
                            state.badges.forEach { badge ->
                                Text("• $badge")
                            }
                        } else {
                            Text("No badges yet. Report issues to earn points!", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text("CivicLens Hackathon MVP v0.1", style = MaterialTheme.typography.labelSmall)
        }
    }
}
