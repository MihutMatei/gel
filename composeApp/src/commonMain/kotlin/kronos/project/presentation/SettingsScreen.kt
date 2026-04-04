package kronos.project.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.Dependencies
import kronos.project.domain.model.SettingsDto
import kronos.project.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel() }
    val loadedSettings by settingsViewModel.settings.collectAsState()
    val loadedProfile by settingsViewModel.profile.collectAsState()
    val saveError by settingsViewModel.error.collectAsState()

    // Current state from backend-backed values with local fallbacks
    val initialDarkMode = loadedSettings?.darkMode ?: Dependencies.isDarkMode.collectAsState().value
    val initialRole = loadedProfile?.role ?: Dependencies.currentUserRole.collectAsState().value

    // Pending changes (local state)
    var pendingDarkMode by remember(initialDarkMode) { mutableStateOf(initialDarkMode) }
    var pendingRole by remember(initialRole) { mutableStateOf(initialRole) }

    val systemInDarkTheme = isSystemInDarkTheme()
    val isCurrentlyDark = pendingDarkMode ?: systemInDarkTheme

    val hasChanges = pendingDarkMode != initialDarkMode || pendingRole != initialRole

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasChanges) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val settings = SettingsDto(
                            darkMode = pendingDarkMode ?: false,
                            notificationsEnabled = loadedSettings?.notificationsEnabled ?: true,
                            language = loadedSettings?.language ?: "en",
                        )
                        val profile = loadedProfile
                        if (profile != null) {
                            settingsViewModel.save(settings, profile.copy(role = pendingRole))
                        }
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("Apply Changes") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!saveError.isNullOrBlank()) {
                Text(
                    text = saveError ?: "Failed to save settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // Theme Section
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent = { Text("Dark Mode") },
                        supportingContent = {
                            Text(
                                if (pendingDarkMode == null) "Following system (${if (systemInDarkTheme) "Dark" else "Light"})"
                                else if (pendingDarkMode == true) "Always dark"
                                else "Always light"
                            )
                        },
                        leadingContent = {
                            Icon(
                                if (isCurrentlyDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isCurrentlyDark,
                                onCheckedChange = { isChecked ->
                                    pendingDarkMode = isChecked
                                }
                            )
                        }
                    )
                    // Option to reset to system default
                    if (pendingDarkMode != null) {
                        TextButton(
                            onClick = { pendingDarkMode = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset to System Default")
                        }
                    }
                }
            }

            // User Role Section
            Text(
                "Account Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    UserRoleOption(
                        role = UserRole.CITIZEN,
                        selected = pendingRole == UserRole.CITIZEN,
                        onClick = { pendingRole = UserRole.CITIZEN }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    UserRoleOption(
                        role = UserRole.TOWNHALL_EMPLOYEE,
                        selected = pendingRole == UserRole.TOWNHALL_EMPLOYEE,
                        onClick = { pendingRole = UserRole.TOWNHALL_EMPLOYEE }
                    )
                }
            }

            // Other Settings (Placeholders as requested)
            Text(
                "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsEntry(Icons.Default.Notifications, "Notifications", "Alerts for status updates")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.Default.PrivacyTip, "Privacy", "Manage your visibility")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.AutoMirrored.Filled.Help, "Help & Support", "Get assistance")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.Default.Info, "About", "CivicLens v0.1")
                }
            }
        }
    }
}

@Composable
fun UserRoleOption(role: UserRole, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(role.name.replace("_", " ")) },
        leadingContent = {
            Icon(
                if (role == UserRole.CITIZEN) Icons.Default.Face else Icons.Default.Work,
                contentDescription = null
            )
        },
        trailingContent = {
            RadioButton(selected = selected, onClick = onClick)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsEntry(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
}
