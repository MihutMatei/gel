package kronos.project.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.Dependencies
import kronos.project.domain.model.GamificationState
import kronos.project.domain.model.UserRole
import kronos.project.ui.theme.shimmerLoadingAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
) {
    val gamificationState by viewModel.gamificationState.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (gamificationState == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Shimmer Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(120.dp).shimmerLoadingAnimation())
                    Box(modifier = Modifier.width(150.dp).height(24.dp).shimmerLoadingAnimation())
                }
                // Shimmer Gamification
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).shimmerLoadingAnimation())
                // Shimmer Stats
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f).height(100.dp).shimmerLoadingAnimation())
                    Box(modifier = Modifier.weight(1f).height(100.dp).shimmerLoadingAnimation())
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    HeaderSection(currentUserRole)
                }

                item {
                    gamificationState?.let { state ->
                        GamificationSection(state)
                    }
                }

                item {
                    StatsSection()
                }

                item {
                    RecentActivitySection()
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "CivicLens Hackathon MVP v0.1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(role: UserRole) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Alex Johnson",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role.name.replace("_", " "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun StatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Reports",
            value = "12",
            icon = Icons.Default.Description,
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Resolved",
            value = "8",
            icon = Icons.Default.TaskAlt,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                ActivityItem("Reported 'Pothole on Main St'", "2 days ago", Icons.Default.AddCircle)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                ActivityItem("Commented on 'Park Bench Broken'", "1 week ago", Icons.AutoMirrored.Filled.Comment)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                ActivityItem("Earned 'First Responder' Badge", "2 weeks ago", Icons.Default.EmojiEvents)
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, date: String, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(date) },
        leadingContent = { Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp)) }
    )
}

@Composable
fun GamificationSection(state: GamificationState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Level and Points Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Level ${state.level}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${state.points} Total Points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            // Animated XP Bar
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val progress = (state.points % 100) / 100f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                )

                val infiniteTransition = rememberInfiniteTransition()
                val shimmerTranslate by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 2000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Progress to Level ${state.level + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = Color(0xFF80CBC4) // Mint/Teal accent

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width * animatedProgress
                        
                        // Main Gradient Progress
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            ),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(size.height / 2)
                        )

                        // Shimmer highlight
                        val shimmerWidth = 200f
                        val xOffset = (shimmerTranslate % (size.width + shimmerWidth)) - shimmerWidth
                        
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0f)
                                ),
                                startX = xOffset,
                                endX = xOffset + shimmerWidth
                            ),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(size.height / 2)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.alpha(0.3f))

            // Badges Grid
            Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            BadgeGrid(state.badges)

            HorizontalDivider(modifier = Modifier.alpha(0.3f))

            // Reporting History Chart
            Text("Reporting History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ReportingHistoryChart(state.monthlyHistory)
        }
    }
}

@Composable
fun BadgeGrid(earnedBadges: List<String>) {
    val allBadges = listOf(
        "First Report" to Icons.Default.CameraAlt,
        "Active Citizen" to Icons.Default.Groups,
        "Community Helper" to Icons.Default.Handshake,
        "Bronze Hero" to Icons.Default.MilitaryTech,
        "Silver Hero" to Icons.Default.WorkspacePremium,
        "Top Contributor" to Icons.Default.Star
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val rows = allBadges.chunked(3)
        rows.forEach { rowBadges ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowBadges.forEach { (name, icon) ->
                    val isEarned = earnedBadges.contains(name)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = if (isEarned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            tonalElevation = if (isEarned) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    icon,
                                    contentDescription = name,
                                    tint = if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Text(
                            name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isEarned) FontWeight.Bold else FontWeight.Normal,
                            color = if (isEarned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
                // Fill empty slots if row is not full
                repeat(3 - rowBadges.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ReportingHistoryChart(history: Map<String, Int>) {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    val data = months.map { history[it] ?: 0 }
    val maxVal = (data.maxOrNull() ?: 1).coerceAtLeast(1)

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = Color(0xFF80CBC4) // Mint/Teal accent
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outlineVariant

    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }

    val animatedValues = data.map { value ->
        animateFloatAsState(
            targetValue = if (animationStarted) value.toFloat() else 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val usableHeight = height - 40f
                val barWidth = (width / months.size) * 0.5f
                val spacing = (width / months.size)

                // Draw Horizontal Grid Lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = usableHeight - (i * usableHeight / gridLines)
                    drawLine(
                        color = outline.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bars
                animatedValues.forEachIndexed { index, animatedValue ->
                    val value = animatedValue.value
                    val barHeight = (value / maxVal) * usableHeight
                    val x = index * spacing + (spacing - barWidth) / 2
                    val y = usableHeight - barHeight

                    if (barHeight > 0) {
                        // Bar Path with rounded top corners only
                        val path = Path().apply {
                            val cornerRadius = 8.dp.toPx()
                            moveTo(x, usableHeight)
                            lineTo(x, y + cornerRadius)
                            quadraticTo(x, y, x + cornerRadius, y)
                            lineTo(x + barWidth - cornerRadius, y)
                            quadraticTo(x + barWidth, y, x + barWidth, y + cornerRadius)
                            lineTo(x + barWidth, usableHeight)
                            close()
                        }

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(colorPrimary, colorSecondary)
                            )
                        )
                        
                        // Glow effect
                        drawPath(
                            path = path,
                            color = colorPrimary.copy(alpha = 0.15f),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
                
                // Baseline
                drawLine(
                    color = outline.copy(alpha = 0.5f),
                    start = Offset(0f, usableHeight),
                    end = Offset(width, usableHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { month ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        month,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection() {
    val isDarkModeSetting by Dependencies.isDarkMode.collectAsState()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
        ListItem(
            headlineContent = { Text("Dark Mode") },
            supportingContent = { Text("Toggle between light and dark") },
            leadingContent = { Icon(if (isDarkModeSetting == true) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null) },
            trailingContent = { 
                Switch(
                    checked = isDarkModeSetting ?: false, // Fallback for system default
                    onCheckedChange = { isChecked ->
                        Dependencies.isDarkMode.value = isChecked
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
        SettingsItem(Icons.Default.Notifications, "Notifications", "Alerts for status updates")
        SettingsItem(Icons.Default.PrivacyTip, "Privacy", "Manage your visibility")
        SettingsItem(Icons.AutoMirrored.Filled.Help, "Help & Support", "Get assistance")
        SettingsItem(Icons.Default.Info, "About", "Learn more about CivicLens")
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
}
