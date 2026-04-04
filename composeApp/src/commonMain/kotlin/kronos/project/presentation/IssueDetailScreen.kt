package kronos.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.domain.model.IssueStatus
import kronos.project.domain.model.UserRole

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
                title = { Text(issue?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(currentIssue.category) }
                            )
                            Text(currentIssue.description, style = MaterialTheme.typography.bodyLarge)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(currentIssue.status.name) }
                                )
                                Text(
                                    "Reported by ${currentIssue.authorRole}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            if (currentUserRole == UserRole.TOWNHALL_EMPLOYEE) {
                                Text("Update Status:", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IssueStatus.values().forEach { status ->
                                        FilterChip(
                                            selected = currentIssue.status == status,
                                            onClick = { viewModel.updateStatus(status) },
                                            label = { Text(status.name) }
                                        )
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (comment.authorRole == UserRole.TOWNHALL_EMPLOYEE) 
                                    MaterialTheme.colorScheme.secondaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    comment.authorRole.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(comment.text)
                            }
                        }
                    }
                }

                Surface(tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Add a comment...") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                viewModel.addComment(commentText)
                                commentText = ""
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
        }
    }
}
