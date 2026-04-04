package kronos.project.domain.model

data class UserProfile(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val points: Int,
    val reports: Int,
    val resolved: Int,
) {
    val displayName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { username }
}

