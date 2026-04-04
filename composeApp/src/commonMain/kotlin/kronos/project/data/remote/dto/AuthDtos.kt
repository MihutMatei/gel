package kronos.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val username: String,
    val email: String,
)

@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val user: AuthUserDto,
)

@Serializable
data class ErrorResponseDto(
    val message: String,
)

