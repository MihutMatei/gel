package kronos.project.dto

import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val language: String,
)

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
data class UpdateUserProfileRequest(
    val firstName: String,
    val lastName: String,
    val role: String,
)

