package kronos.project.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kronos.project.map.MapMarkerCard

@Immutable
data class IssueThreadPostUi(
    val author: String,
    val content: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val timestampLabel: String = "now",
)

@Immutable
data class IssueThreadCommentUi(
    val id: String,
    val author: String,
    val content: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val depth: Int = 0,
    val timestampLabel: String = "now",
)

@Immutable
data class IssueThreadPreviewUi(
    val title: String,
    val metadata: String,
    val mainPost: IssueThreadPostUi,
    val comments: List<IssueThreadCommentUi>,
)

@Composable
fun IssueThreadPopup(
    thread: IssueThreadPreviewUi,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .width(320.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = thread.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = thread.metadata,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close discussion popup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            MainPostSurface(post = thread.mainPost)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Text(
                text = "Comments",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 220.dp),
                contentPadding = PaddingValues(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(thread.comments, key = { it.id }) { comment ->
                    ThreadCommentRow(comment = comment)
                }
            }
        }
    }
}

@Composable
private fun MainPostSurface(post: IssueThreadPostUi) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "u/${post.author}  +${post.upvotes}/-${post.downvotes}  -  ${post.timestampLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ThreadCommentRow(comment: IssueThreadCommentUi) {
    val indent = (comment.depth.coerceIn(0, 3) * 12).dp
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 8.dp)
                .width(2.dp)
                .background(guideColor, RoundedCornerShape(2.dp))
                .heightIn(min = 34.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "u/${comment.author}  +${comment.upvotes}/-${comment.downvotes}  -  ${comment.timestampLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Stable
fun MapMarkerCard.toIssueThreadPreviewUi(): IssueThreadPreviewUi {
    return IssueThreadPreviewUi(
        title = title,
        metadata = "posted by u/${mainPost.author}",
        mainPost = IssueThreadPostUi(
            author = mainPost.author,
            content = mainPost.content,
            upvotes = mainPost.upvotes,
            downvotes = mainPost.downvotes,
        ),
        comments = comments.mapIndexed { index, comment ->
            IssueThreadCommentUi(
                id = "comment-$index",
                author = comment.author,
                content = comment.content,
                upvotes = comment.upvotes,
                downvotes = comment.downvotes,
                depth = 0,
            )
        },
    )
}

