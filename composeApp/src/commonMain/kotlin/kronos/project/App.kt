package kronos.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kronos.project.domain.model.AuthState
import kronos.project.presentation.*
import kronos.project.ui.theme.CivicLensTheme
import kronos.project.util.changeLanguage

@Serializable data class CreateIssue(val lat: String, val lon: String)
@Serializable data class IssueDetail(val id: String)
@Serializable object Map
@Serializable object Profile
@Serializable object Settings
@Serializable object Login
@Serializable object Register

@Composable
fun App() {
    val isDarkModeSetting by Dependencies.isDarkMode.collectAsState()
    val darkTheme = isDarkModeSetting ?: isSystemInDarkTheme()
    val language by Dependencies.currentLanguage.collectAsState()

    // We apply the language change BEFORE the UI tries to recompose with it
    changeLanguage(language.code)
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val authState by authViewModel.authState.collectAsState()
    val authError by authViewModel.error.collectAsState()

    CivicLensTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val startDestination: Any = when (authState) {
            AuthState.Loading -> Login
            AuthState.Unauthenticated -> Login
            is AuthState.Authenticated -> Map
        }

        LaunchedEffect(authState) {
            when (authState) {
                AuthState.Loading -> Unit
                AuthState.Unauthenticated -> navController.navigate(Login) {
                    popUpTo<Login> { inclusive = true }
                }
                is AuthState.Authenticated -> navController.navigate(Map) {
                    popUpTo<Login> { inclusive = true }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable<Login> {
                LoginScreen(
                    error = authError,
                    onLogin = { email, password -> authViewModel.login(email, password) },
                    onGoToRegister = { navController.navigate(Register) },
                )
            }
            composable<Register> {
                RegisterScreen(
                    error = authError,
                    onRegister = { username, firstName, lastName, email, password ->
                        authViewModel.register(username, firstName, lastName, email, password)
                    },
                    onGoToLogin = { navController.popBackStack() },
                )
            }
            composable<Map> {
                key(language) {
                    MapScreen(
                        onIssueClick = { id -> navController.navigate(IssueDetail(id)) },
                        onCreateIssue = { lat, lon -> navController.navigate(CreateIssue(lat.toString(), lon.toString())) },
                        onProfileClick = { navController.navigate(Profile) }
                    )
                }
            }
            composable<CreateIssue>(
                enterTransition = {
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)) + fadeIn()
                },
                exitTransition = {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500)) + fadeOut()
                },
                popEnterTransition = { fadeIn() },
                popExitTransition = {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500)) + fadeOut()
                }
            ) { backStackEntry ->
                key(language) {
                    val args: CreateIssue = backStackEntry.toRoute()
                    CreateIssueScreen(
                        latitude = args.lat,
                        longitude = args.lon,
                        onBack = { navController.popBackStack() },
                        onIssueCreated = { navController.popBackStack() }
                    )
                }
            }
            composable<IssueDetail>(
                enterTransition = {
                    fadeIn(animationSpec = tween(600)) + expandHorizontally()
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(600)) + shrinkHorizontally()
                }
            ) { backStackEntry ->
                key(language) {
                    val args: IssueDetail = backStackEntry.toRoute()
                    IssueDetailScreen(
                        issueId = args.id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable<Profile> {
                key(language) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSettingsClick = { navController.navigate(Settings) },
                        onLogout = { authViewModel.logout() },
                    )
                }
            }
            composable<Settings> {
                key(language) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}