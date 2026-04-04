package kronos.project.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kronos.project.models.PinStatus

@Serializable
enum class PinStatusDto {
    @SerialName("open")
    OPEN,

    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("resolved")
    RESOLVED,
}

@Serializable
data class CreatePinRequest(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val createdBy: String,
)

@Serializable
data class UpdatePinRequest(
    val description: String? = null,
    val status: PinStatusDto? = null,
)

@Serializable
data class PinImageResponse(
    val id: String,
    val pinId: String,
    val imageUrl: String,
    val uploadedAt: String,
)

@Serializable
data class PinSummaryResponse(
    val id: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val status: PinStatusDto,
    val createdBy: String,
    val createdAt: String,
)

@Serializable
data class PinDetailsResponse(
    val pin: PinSummaryResponse,
    val comments: List<CommentResponse>,
    val images: List<PinImageResponse>,
)

@Serializable
data class ErrorResponse(val message: String)

fun PinStatusDto.toModel(): PinStatus = when (this) {
    PinStatusDto.OPEN -> PinStatus.OPEN
    PinStatusDto.IN_PROGRESS -> PinStatus.IN_PROGRESS
    PinStatusDto.RESOLVED -> PinStatus.RESOLVED
}

fun PinStatus.toDto(): PinStatusDto = when (this) {
    PinStatus.OPEN -> PinStatusDto.OPEN
    PinStatus.IN_PROGRESS -> PinStatusDto.IN_PROGRESS
    PinStatus.RESOLVED -> PinStatusDto.RESOLVED
}

