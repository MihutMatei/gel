package kronos.project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val language: String,
)

