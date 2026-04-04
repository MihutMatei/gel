package kronos.project.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.data.remote.dto.CommentDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintThreadSheet(
    pinId: String,
    onDismiss: () -> Unit,
    viewModel: ComplaintThreadViewModel = viewModel { ComplaintThreadViewModel() },
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(pinId) {
        viewModel.loadThread(pinId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Complaint Thread",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Sort order toggle
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = sortOrder == CommentSortOrder.NEW,
                    onClick = { viewModel.setSortOrder(CommentSortOrder.NEW) },
                    label = { Text("New") },
                    leadingIcon = if (sortOrder == CommentSortOrder.NEW) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
                FilterChip(
                    selected = sortOrder == CommentSortOrder.TOP,
                    onClick = { viewModel.setSortOrder(CommentSortOrder.TOP) },
                    label = { Text("Top") },
                    leadingIcon = if (sortOrder == CommentSortOrder.TOP) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            when {
                loading -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                !error.isNullOrBlank() -> Text(
                    text = error ?: "Error loading comments",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 480.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Text(
                                    "No comments yet. Be the first to reply!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                                )
                            }
                        } else {
                            items(comments, key = { it.id }) { comment ->
                                CommentThreadItem(
                                    comment = comment,
                                    pinId = pinId,
                                    depth = 0,
                                    onVote = { commentId, up -> viewModel.voteOnComment(pinId, commentId, up) },
                                    onReply = { parentId, text -> viewModel.postComment(pinId, text, parentId) },
                                )
                            }
                        }
                    }

                    CommentInputBar(
                        onSubmit = { text -> viewModel.postComment(pinId, text) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentThreadItem(
    comment: CommentDto,
    pinId: String,
    depth: Int,
    onVote: (commentId: String, isUpvote: Boolean) -> Unit,
    onReply: (parentId: String, text: String) -> Unit,
) {
    var showReplyInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (depth == 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "User ${comment.authorId.take(8)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = comment.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                        onClick = { onVote(comment.id, true) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = "${comment.upvotes}",
                        style = MaterialTheme.typography.labelSmall,
                    )

                    IconButton(
                        onClick = { onVote(comment.id, false) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.ThumbDown,
                            contentDescription = "Downvote",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "${comment.downvotes}",
                        style = MaterialTheme.typography.labelSmall,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (depth < 3) {
                        TextButton(
                            onClick = { showReplyInput = !showReplyInput },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(
                                Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reply", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (showReplyInput) {
            CommentInputBar(
                placeholder = "Write a reply...",
                onSubmit = { text ->
                    onReply(comment.id, text)
                    showReplyInput = false
                },
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }

        if (comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            comment.replies.forEach { reply ->
                CommentThreadItem(
                    comment = reply,
                    pinId = pinId,
                    depth = depth + 1,
                    onVote = onVote,
                    onReply = onReply,
                )
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    placeholder: String = "Add a comment...",
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(placeholder) },
            modifier = Modifier.weight(1f),
            maxLines = 3,
            shape = RoundedCornerShape(24.dp),
        )
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSubmit(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
    }
}
