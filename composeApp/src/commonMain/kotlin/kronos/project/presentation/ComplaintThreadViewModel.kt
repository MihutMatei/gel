package kronos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kronos.project.Dependencies
import kronos.project.data.remote.dto.CommentDto
import kronos.project.data.remote.dto.CreateCommentDto
import kronos.project.data.remote.dto.VoteCommentDto

enum class CommentSortOrder { TOP, NEW }

class ComplaintThreadViewModel : ViewModel() {
    private val pinRepository = Dependencies.pinRepository

    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    private val _sortOrder = MutableStateFlow(CommentSortOrder.NEW)
    val sortOrder: StateFlow<CommentSortOrder> = _sortOrder.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    fun loadThread(pinId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            pinRepository.fetchComments(pinId)
                .onSuccess { raw ->
                    _comments.value = sortComments(raw, _sortOrder.value)
                }
                .onFailure { _error.value = it.message ?: "Failed to load comments" }

            _loading.value = false
        }
    }

    fun setSortOrder(order: CommentSortOrder) {
        _sortOrder.value = order
        _comments.value = sortComments(_comments.value, order)
    }

    fun postComment(pinId: String, content: String, parentId: String? = null) {
        val userId = Dependencies.currentUser.value?.id ?: return
        viewModelScope.launch {
            _submitSuccess.value = false
            val request = CreateCommentDto(authorId = userId, content = content, parentId = parentId)
            val result = if (parentId != null) {
                pinRepository.replyToComment(parentId, request)
            } else {
                pinRepository.addComment(pinId, request)
            }
            result
                .onSuccess {
                    loadThread(pinId)
                    _submitSuccess.value = true
                }
                .onFailure { _error.value = it.message ?: "Failed to post comment" }
        }
    }

    fun voteOnComment(pinId: String, commentId: String, isUpvote: Boolean) {
        val userId = Dependencies.currentUser.value?.id ?: return
        viewModelScope.launch {
            pinRepository.voteOnComment(commentId, VoteCommentDto(userId = userId, isUpvote = isUpvote))
                .onSuccess { loadThread(pinId) }
                .onFailure { _error.value = it.message ?: "Vote failed" }
        }
    }

    private fun sortComments(list: List<CommentDto>, order: CommentSortOrder): List<CommentDto> {
        val sorted = when (order) {
            CommentSortOrder.TOP -> list.sortedByDescending { it.upvotes - it.downvotes }
            // ISO-8601 timestamps (e.g. "2025-01-01T12:00:00Z") sort correctly as strings
            CommentSortOrder.NEW -> list.sortedByDescending { it.createdAt }
        }
        return sorted.map { comment ->
            comment.copy(replies = sortComments(comment.replies, order))
        }
    }
}
