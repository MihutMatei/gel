package kronos.project.domain.model

data class AuthUser(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
)

