package app.kitsune.stream.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.kitsune.stream.R
import app.kitsune.stream.ui.components.KitsuneBottomBar
import app.kitsune.stream.ui.components.KitsuneTab
import app.kitsune.stream.ui.screens.DetailScreen
import app.kitsune.stream.ui.screens.HomeScreen
import app.kitsune.stream.ui.screens.SearchScreen
import app.kitsune.stream.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{anilistId}"
    fun detail(anilistId: Int) = "detail/$anilistId"
}

@Composable
fun KitsuneApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabs = listOf(
        KitsuneTab(Routes.HOME, Icons.Filled.Home, R.string.tab_home),
        KitsuneTab(Routes.SEARCH, Icons.Filled.Search, R.string.tab_search),
        KitsuneTab(Routes.SETTINGS, Icons.Filled.Settings, R.string.tab_settings)
    )
    val showBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                KitsuneBottomBar(tabs, currentRoute) { route ->
                    nav.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(onAnimeClick = { nav.navigate(Routes.detail(it)) })
            }
            composable(Routes.SEARCH) {
                SearchScreen(onAnimeClick = { nav.navigate(Routes.detail(it)) })
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("anilistId") { type = NavType.IntType })
            ) {
                DetailScreen(
                    onBack = { nav.popBackStack() },
                    onAnimeClick = { nav.navigate(Routes.detail(it)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
