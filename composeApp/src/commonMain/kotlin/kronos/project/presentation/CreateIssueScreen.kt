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
import gel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import gel.composeapp.generated.resources.*
import kronos.project.util.getCategoryResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    latitude: String,
    longitude: String,
    onBack: () -> Unit,
    onIssueCreated: () -> Unit,
    viewModel: CreateIssueViewModel = viewModel { CreateIssueViewModel() }
) {
    val lat = latitude.toDoubleOrNull() ?: 0.0
    val lon = longitude.toDoubleOrNull() ?: 0.0
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var categoryId by remember { mutableStateOf("public_transport") }
    val categoryIds = remember {
        listOf(
            "public_transport",
            "utilities",
            "parking",
            "crime_safety",
            "commerce_store_access",
            "road_hazards",
            "lighting",
            "sanitation",
            "infrastructure",
            "other"
        )
    }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.report_new_issue)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
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
                stringResource(Res.string.provide_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 50) title = it },
                label = { Text(stringResource(Res.string.short_title)) },
                placeholder = { Text(stringResource(Res.string.title_placeholder)) },
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
                            Text(stringResource(Res.string.title_too_short_full), color = MaterialTheme.colorScheme.error)
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
                    value = stringResource(getCategoryResource(categoryId)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(Res.string.category)) },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoryIds.forEach { id ->
                        DropdownMenuItem(
                            text = { Text(stringResource(getCategoryResource(id))) },
                            onClick = {
                                categoryId = id
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    when (id) {
                                        "public_transport" -> Icons.Default.DirectionsBus
                                        "utilities" -> Icons.Default.WaterDrop
                                        "parking" -> Icons.Default.LocalParking
                                        "crime_safety" -> Icons.Default.Security
                                        "commerce_store_access" -> Icons.Default.Store
                                        "road_hazards" -> Icons.Default.Warning
                                        "lighting" -> Icons.Default.Lightbulb
                                        "sanitation" -> Icons.Default.Delete
                                        "infrastructure" -> Icons.Default.Architecture
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
                label = { Text(stringResource(Res.string.detailed_description)) },
                placeholder = { Text(stringResource(Res.string.description_impact)) },
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
                            Text(stringResource(Res.string.description_too_short_full), color = MaterialTheme.colorScheme.error)
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
                stringResource(Res.string.add_photos_optional),
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
                        contentDescription = stringResource(Res.string.add_photos),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(Res.string.tap_to_add_photos),
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
                        Text(stringResource(Res.string.tagged_location), style = MaterialTheme.typography.labelLarge)
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
                                viewModel.createIssue(title, description, categoryId, lat, lon)
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
                    Text(stringResource(Res.string.submit_report), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
