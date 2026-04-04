package kronos.project.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    latitude: Double,
    longitude: Double,
    onBack: () -> Unit,
    onIssueCreated: () -> Unit,
    viewModel: CreateIssueViewModel = viewModel { CreateIssueViewModel() }
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Public transport") }
    val categories = listOf("Public transport", "Utilities", "Parking", "Crime / safety", "Commerce / store access")
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report New Issue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Provide details about the issue you've encountered.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                label = { Text("Issue Name") },
                placeholder = { Text("e.g., Pothole on Main St") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                trailingIcon = {
                    if (title.length >= 5) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = MaterialTheme.colorScheme.primary)
                    } else if (title.isNotEmpty()) {
                        Icon(Icons.Default.Error, contentDescription = "Too short", tint = MaterialTheme.colorScheme.error)
                    }
                },
                supportingText = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (title.isNotEmpty() && title.length < 5) {
                            Text("Title too short (min 5 chars)", color = MaterialTheme.colorScheme.error)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text("${title.length}/50")
                    }
                },
                isError = title.isNotEmpty() && title.length < 5,
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type of Problem") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    when (cat) {
                                        "Public transport" -> Icons.Default.DirectionsBus
                                        "Utilities" -> Icons.Default.WaterDrop
                                        "Parking" -> Icons.Default.LocalParking
                                        "Crime / safety" -> Icons.Default.Security
                                        "Commerce / store access" -> Icons.Default.Store
                                        else -> Icons.AutoMirrored.Filled.Label
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                label = { Text("Description") },
                placeholder = { Text("Describe what's wrong and its impact...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                leadingIcon = {
                    Box(modifier = Modifier.padding(bottom = 60.dp)) { // Align icon to top
                        Icon(Icons.Default.Description, contentDescription = null)
                    }
                },
                trailingIcon = {
                    if (description.length >= 20) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = MaterialTheme.colorScheme.primary)
                    } else if (description.isNotEmpty()) {
                        Icon(Icons.Default.Error, contentDescription = "Too short", tint = MaterialTheme.colorScheme.error)
                    }
                },
                supportingText = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (description.isNotEmpty() && description.length < 20) {
                            Text("Description too short (min 20 chars)", color = MaterialTheme.colorScheme.error)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text("${description.length}/500")
                    }
                },
                isError = description.isNotEmpty() && description.length < 20
            )

            // Photo Picker Placeholder
            Text(
                "Add Photos (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { /* Open photo picker */ },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add Photos",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to add photos or drag and drop",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Tagged Location", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${latitude.toString().take(8)}, ${longitude.toString().take(8)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            var loading by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(if (isPressed) 0.95f else 1f)

            Button(
                onClick = {
                    if (!loading) {
                        loading = true
                        scope.launch {
                            try {
                                viewModel.createIssue(title, description, category, latitude, longitude)
                                onIssueCreated()
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale),
                enabled = title.length >= 5 && description.length >= 20 && !loading,
                shape = MaterialTheme.shapes.medium,
                interactionSource = interactionSource
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Report", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
