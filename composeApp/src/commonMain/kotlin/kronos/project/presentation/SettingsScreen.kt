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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kronos.project.Dependencies
import kronos.project.Language
import kronos.project.domain.model.UserRole
import gel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // Current state from Dependencies
    val currentDarkMode by Dependencies.isDarkMode.collectAsState()
    val currentRole by Dependencies.currentUserRole.collectAsState()
    val currentLanguage by Dependencies.currentLanguage.collectAsState()

    // Local state for pending changes
    var pendingDarkMode by remember { mutableStateOf(currentDarkMode) }
    var pendingRole by remember { mutableStateOf(currentRole) }
    var pendingLanguage by remember { mutableStateOf(currentLanguage) }

    // Synchronize local state when global state changes EXTERNALLY (if needed)
    // But since this is a "save changes" screen, we want to keep current selections until Apply
    LaunchedEffect(currentDarkMode) { pendingDarkMode = currentDarkMode }
    LaunchedEffect(currentRole) { pendingRole = currentRole }
    LaunchedEffect(currentLanguage) { pendingLanguage = currentLanguage }

    val systemInDarkTheme = isSystemInDarkTheme()
    val isCurrentlyDark = pendingDarkMode ?: systemInDarkTheme

    val hasChanges = pendingDarkMode != currentDarkMode || pendingRole != currentRole || pendingLanguage != currentLanguage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasChanges) {
                ExtendedFloatingActionButton(
                    onClick = {
                        Dependencies.isDarkMode.value = pendingDarkMode
                        Dependencies.currentUserRole.value = pendingRole
                        Dependencies.currentLanguage.value = pendingLanguage
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text(stringResource(Res.string.apply_changes)) },
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
            // Theme Section
            Text(
                stringResource(Res.string.theme),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.dark_mode)) },
                        supportingContent = {
                            val modeName = if (systemInDarkTheme) stringResource(Res.string.dark) else stringResource(Res.string.light)
                            val text = if (pendingDarkMode == null) stringResource(Res.string.following_system, modeName)
                            else if (pendingDarkMode == true) stringResource(Res.string.always_dark)
                            else stringResource(Res.string.always_light)
                            Text(text)
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
                            Text(stringResource(Res.string.reset_to_system_default))
                        }
                    }
                }
            }

            // User Role Section
            Text(
                stringResource(Res.string.account_type),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    UserRoleOption(
                        role = UserRole.CITIZEN,
                        selected = pendingRole == UserRole.CITIZEN,
                        language = pendingLanguage,
                        onClick = { pendingRole = UserRole.CITIZEN }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    UserRoleOption(
                        role = UserRole.TOWNHALL_EMPLOYEE,
                        selected = pendingRole == UserRole.TOWNHALL_EMPLOYEE,
                        language = pendingLanguage,
                        onClick = { pendingRole = UserRole.TOWNHALL_EMPLOYEE }
                    )
                }
            }

            // Other Settings (Placeholders as requested)
            Text(
                stringResource(Res.string.preferences),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    var showLanguagePicker by remember { mutableStateOf(false) }

                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.language)) },
                        supportingContent = { Text(pendingLanguage.displayName) },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showLanguagePicker = true }
                    )

                    if (showLanguagePicker) {
                        AlertDialog(
                            onDismissRequest = { showLanguagePicker = false },
                            title = { Text(stringResource(Res.string.select_language)) },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Language.values().forEach { lang ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    pendingLanguage = lang
                                                    showLanguagePicker = false 
                                                }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = lang == pendingLanguage, onClick = null)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(lang.displayName)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showLanguagePicker = false }) {
                                    Text(stringResource(Res.string.close))
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.Default.Notifications, stringResource(Res.string.notifications), stringResource(Res.string.alerts_status_updates), pendingLanguage)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.Default.PrivacyTip, stringResource(Res.string.privacy), stringResource(Res.string.manage_visibility), pendingLanguage)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.AutoMirrored.Filled.Help, stringResource(Res.string.help_support), stringResource(Res.string.get_assistance), pendingLanguage)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    SettingsEntry(Icons.Default.Info, stringResource(Res.string.about), stringResource(Res.string.version_info), pendingLanguage)
                }
            }
        }
    }
}

@Composable
fun UserRoleOption(role: UserRole, selected: Boolean, language: Language, onClick: () -> Unit) {
    val roleName = if (role == UserRole.CITIZEN) stringResource(Res.string.citizen) else stringResource(Res.string.townhall_employee)
    ListItem(
        headlineContent = { Text(roleName) },
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
private fun SettingsEntry(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, language: Language) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
}
