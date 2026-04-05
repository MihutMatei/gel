package kronos.project

import kotlinx.coroutines.flow.MutableStateFlow
import kronos.project.data.remote.AppHttpClient
import kronos.project.data.remote.TokenStorage
import kronos.project.data.repository.*
import kronos.project.domain.model.UserRole
import kronos.project.domain.usecase.*
import kronos.project.domain.usecase.CreateIssue

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ROMANIAN("ro", "Română"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    SPANISH("es", "Español"),
    MANDARIN("zh", "中文"),
    RUSSIAN("ru", "Русский"),
    JAPANESE("ja", "日本語"),
    HINDI("hi", "हिन्दी"),
    URDU("ur", "اردو"),
    VIETNAMESE("vi", "Tiếng Việt"),
    INDONESIAN("id", "Bahasa Indonesia")
}

object Dependencies {
    val currentUserRole = MutableStateFlow(UserRole.CITIZEN)
    val currentUserId = MutableStateFlow<String?>(null)
    val isDarkMode = MutableStateFlow<Boolean?>(null) // null means follow system
    val currentLanguage = MutableStateFlow(Language.ENGLISH)

    val tokenStorage = TokenStorage()
    val httpClient = AppHttpClient.create(tokenStorage)
    val authRepository = AuthRepository(httpClient, tokenStorage)
    val pinRepository = PinRepository(httpClient)
    val settingsRepository = SettingsRepository(httpClient)
    val userRepository = UserRepository(httpClient)

    val issueRepository = FakeIssueRepository()
    val commentRepository = FakeCommentRepository()
    val gamificationRepository = FakeGamificationRepository()

    val getIssues = GetIssues(issueRepository)
    val getIssueById = GetIssueById(issueRepository)
    val createIssue = CreateIssue(issueRepository)
    val updateIssueStatus = UpdateIssueStatus(issueRepository)
    val addComment = AddComment(commentRepository)
    val awardPointsForIssue = AwardPointsForIssue(gamificationRepository)
}
