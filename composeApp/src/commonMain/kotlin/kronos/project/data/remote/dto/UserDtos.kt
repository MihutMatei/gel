package kronos.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val points: Int,
    val reports: Int,
    val resolved: Int,
)

@Serializable
data class UpdateUserProfileRequestDto(
    val firstName: String,
    val lastName: String,
    val role: String,
)

