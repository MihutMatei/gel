package kronos.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole
import kronos.project.ui.theme.shimmerLoadingAnimation
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    onBack: () -> Unit,
    viewModel: IssueDetailViewModel = viewModel { IssueDetailViewModel(issueId) }
) {
    val issue by viewModel.issue.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(issue?.title ?: "Loading Issue...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (issue == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shimmer for the main card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shimmerLoadingAnimation()
                )
                // Shimmer for title/section
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(24.dp)
                        .shimmerLoadingAnimation()
                )
                // Shimmers for comments
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .shimmerLoadingAnimation()
                    )
                }
            }
        } else {
            issue?.let { currentIssue ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(currentIssue.category) },
                                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) }
                                        )
                                        StatusBadge(currentIssue.status)
                                    }

                                    Text(
                                        currentIssue.description,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        MetadataRow(
                                            icon = Icons.Default.Person,
                                            label = "Reported by",
                                            value = currentIssue.authorRole.name
                                        )
                                        MetadataRow(
                                            icon = Icons.Default.LocationOn,
                                            label = "Location",
                                            value = "${currentIssue.latitude.toString().take(7)}, ${currentIssue.longitude.toString().take(7)}"
                                        )
                                        MetadataRow(
                                            icon = Icons.Default.Event,
                                            label = "Reported on",
                                            value = formatInstant(currentIssue.createdAt)
                                        )
                                    }

                                    if (currentUserRole == UserRole.TOWNHALL_EMPLOYEE) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Update Status", style = MaterialTheme.typography.labelLarge)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            IssueStatus.entries.forEach { status ->
                                                FilterChip(
                                                    selected = currentIssue.status == status,
                                                    onClick = { viewModel.updateStatus(status) },
                                                    label = { Text(status.name.replace("_", " ")) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Discussion", style = MaterialTheme.typography.titleMedium)
                        }

                        items(comments) { comment ->
                            val isTownHall = comment.authorRole == UserRole.TOWNHALL_EMPLOYEE
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isTownHall) Alignment.Start else Alignment.End
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isTownHall) 4.dp else 16.dp,
                                        bottomEnd = if (isTownHall) 16.dp else 4.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isTownHall)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            if (isTownHall) "Town Hall Official" else "Citizen",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTownHall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    Surface(tonalElevation = 2.dp) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .imePadding(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Add a comment...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            FilledIconButton(
                                onClick = {
                                    viewModel.addComment(commentText)
                                    commentText = ""
                                },
                                enabled = commentText.isNotBlank(),
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: IssueStatus) {
    val containerColor = when (status) {
        IssueStatus.OPEN -> MaterialTheme.colorScheme.errorContainer
        IssueStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        IssueStatus.RESOLVED -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when (status) {
        IssueStatus.OPEN -> MaterialTheme.colorScheme.onErrorContainer
        IssueStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onTertiaryContainer
        IssueStatus.RESOLVED -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetadataRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatInstant(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
