package kronos.project

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.toRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import kronos.project.domain.model.AuthState
import kronos.project.presentation.*
import kronos.project.ui.theme.CivicLensTheme

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
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val authState by authViewModel.authState.collectAsState()
    val authError by authViewModel.error.collectAsState()

    CivicLensTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val startDestination: Any = Login

        LaunchedEffect(authState) {
            when (authState) {
                AuthState.Loading -> Unit
                AuthState.Unauthenticated -> navController.navigate(Login) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
                is AuthState.Authenticated -> navController.navigate(Map) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
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
                MapScreen(
                    onIssueClick = { id -> navController.navigate(IssueDetail(id)) },
                    onCreateIssue = { lat, lon -> navController.navigate(CreateIssue(lat.toString(), lon.toString())) },
                    onProfileClick = { navController.navigate(Profile) }
                )
            }
            composable<CreateIssue>(
                enterTransition = { null },
                exitTransition = { null },
                popEnterTransition = { null },
                popExitTransition = { null },
            ) { backStackEntry ->
                val args: CreateIssue = backStackEntry.toRoute()
                CreateIssueScreen(
                    latitude = args.lat.toDoubleOrNull() ?: 0.0,
                    longitude = args.lon.toDoubleOrNull() ?: 0.0,
                    onBack = { navController.popBackStack() },
                    onIssueCreated = { navController.popBackStack() }
                )
            }
            composable<IssueDetail>(
                enterTransition = { null },
                exitTransition = { null },
            ) { backStackEntry ->
                val args: IssueDetail = backStackEntry.toRoute()
                IssueDetailScreen(
                    issueId = args.id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Profile> {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(Settings) },
                    onLogout = { authViewModel.logout() },
                )
            }
            composable<Settings> {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}