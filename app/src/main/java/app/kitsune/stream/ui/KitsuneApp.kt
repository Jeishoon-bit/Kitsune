package app.kitsune.stream.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.kitsune.stream.ui.screens.DetailScreen
import app.kitsune.stream.ui.screens.HomeScreen
import app.kitsune.stream.ui.screens.SearchScreen
import app.kitsune.stream.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail/{malId}"
    const val SETTINGS = "settings"
    fun detail(malId: Int) = "detail/$malId"
}

@Composable
fun KitsuneApp() {
    val nav = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAnimeClick = { nav.navigate(Routes.detail(it)) },
                    onSearchClick = { nav.navigate(Routes.SEARCH) },
                    onSettingsClick = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(onAnimeClick = { nav.navigate(Routes.detail(it)) })
            }
            composable(Routes.DETAIL) { entry ->
                val malId = entry.arguments?.getString("malId")?.toIntOrNull() ?: return@composable
                DetailScreen(malId = malId, onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
