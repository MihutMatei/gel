package kronos.project

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import org.jetbrains.compose.resources.stringResource
import gel.composeapp.generated.resources.*
import androidx.navigation.NavType
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kronos.project.presentation.*
import kronos.project.util.changeLanguage
import kronos.project.ui.theme.CivicLensTheme
import kotlin.reflect.typeOf

@Serializable data class CreateIssue(val lat: String, val lon: String)
@Serializable data class IssueDetail(val id: String)
@Serializable object Map
@Serializable object Profile
@Serializable object Settings

@Composable
fun App() {
    val isDarkModeSetting by Dependencies.isDarkMode.collectAsState()
    val darkTheme = isDarkModeSetting ?: isSystemInDarkTheme()
    val language by Dependencies.currentLanguage.collectAsState()

    // We apply the language change BEFORE the UI tries to recompose with it
    changeLanguage(language.code)

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
                key(language) {
                    MapScreen(
                        onIssueClick = { id -> navController.navigate(IssueDetail(id)) },
                        onCreateIssue = { lat, lon -> navController.navigate(CreateIssue(lat, lon)) },
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
                        onSettingsClick = { navController.navigate(Settings) }
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