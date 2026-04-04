package kronos.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kronos.project.presentation.*
import kronos.project.ui.theme.CivicLensTheme

@Serializable data class CreateIssue(val lat: Double, val lon: Double)
@Serializable data class IssueDetail(val id: String)
@Serializable object Map
@Serializable object Profile
@Serializable object Settings

@Composable
fun App() {
    val isDarkModeSetting by Dependencies.isDarkMode.collectAsState()
    val darkTheme = isDarkModeSetting ?: isSystemInDarkTheme()

    CivicLensTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Map,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable<Map> {
                MapScreen(
                    onIssueClick = { id -> navController.navigate(IssueDetail(id)) },
                    onCreateIssue = { lat, lon -> navController.navigate(CreateIssue(lat, lon)) },
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
                    latitude = args.lat,
                    longitude = args.lon,
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
                    onSettingsClick = { navController.navigate(Settings) }
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