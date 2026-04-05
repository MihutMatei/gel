package kronos.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PinDto(
    val id: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val status: String,
    val createdBy: String,
    val createdAt: String,
)

@Serializable
data class CreatePinRequestDto(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val createdBy: String,
)

@Serializable
data class CreatePinCommentRequestDto(
    val authorId: String,
    val content: String,
)

@Serializable
data class PinCommentDto(
    val id: String,
    val pinId: String,
    val authorId: String,
    val content: String,
    val createdAt: String,
)

