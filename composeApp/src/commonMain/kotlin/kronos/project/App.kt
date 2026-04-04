package kronos.project

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kronos.project.presentation.*
import kronos.project.ui.theme.CivicLensTheme

@Composable
fun App() {
    CivicLensTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "map"
        ) {
            composable("map") {
                MapScreen(
                    onIssueClick = { id -> navController.navigate("issue_detail/$id") },
                    onCreateIssue = { lat, lon -> navController.navigate("create_issue/$lat/$lon") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
            composable(
                route = "create_issue/{lat}/{lon}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lon") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                CreateIssueScreen(
                    latitude = lat,
                    longitude = lon,
                    onBack = { navController.popBackStack() },
                    onIssueCreated = { navController.popBackStack() }
                )
            }
            composable(
                route = "issue_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                IssueDetailScreen(
                    issueId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}