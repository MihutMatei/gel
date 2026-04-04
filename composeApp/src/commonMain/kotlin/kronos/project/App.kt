package kronos.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Serializable object LoadingRoute
@Serializable data class CreateIssue(val lat: String, val lon: String)
@Serializable data class IssueDetail(val id: String)
@Serializable data class ComplaintThread(val pinId: String)
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
        val startDestination: Any = when (authState) {
            AuthState.Loading -> LoadingRoute
            AuthState.Unauthenticated -> Login
            is AuthState.Authenticated -> Map
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LaunchedEffect(authState) {
                when (authState) {
                    AuthState.Loading -> Unit
                    AuthState.Unauthenticated -> navController.navigate(Login) {
                        popUpTo<LoadingRoute> { inclusive = true }
                    }
                    is AuthState.Authenticated -> navController.navigate(Map) {
                        popUpTo<LoadingRoute> { inclusive = true }
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
                composable<LoadingRoute> {
                    LoadingScreen()
                }
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
                        onRegister = { username, email, password -> authViewModel.register(username, email, password) },
                        onGoToLogin = { navController.popBackStack() },
                    )
                }
                composable<Map> {
                    MapScreen(
                        onIssueClick = { id -> navController.navigate(ComplaintThread(id)) },
                        onCreateIssue = { lat, lon -> navController.navigate(CreateIssue(lat.toString(), lon.toString())) },
                        onProfileClick = { navController.navigate(Profile) }
                    )
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
                    val args: CreateIssue = backStackEntry.toRoute()
                    CreateIssueScreen(
                        latitude = args.lat.toDoubleOrNull() ?: 0.0,
                        longitude = args.lon.toDoubleOrNull() ?: 0.0,
                        onBack = { navController.popBackStack() },
                        onIssueCreated = { navController.popBackStack() }
                    )
                }
                composable<IssueDetail>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(600)) + expandHorizontally()
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(600)) + shrinkHorizontally()
                    }
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
                composable<ComplaintThread> { backStackEntry ->
                    val args: ComplaintThread = backStackEntry.toRoute()
                    ComplaintThreadSheet(
                        pinId = args.pinId,
                        onDismiss = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}