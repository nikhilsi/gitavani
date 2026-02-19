package com.nikhilsi.gitavani.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nikhilsi.gitavani.ui.chapters.ChapterDetailScreen
import com.nikhilsi.gitavani.ui.chapters.ChapterListScreen
import com.nikhilsi.gitavani.ui.common.HelpScreen
import com.nikhilsi.gitavani.ui.favorites.FavoritesScreen
import com.nikhilsi.gitavani.ui.settings.AboutScreen
import com.nikhilsi.gitavani.ui.settings.SettingsScreen
import com.nikhilsi.gitavani.ui.verses.VerseDetailScreen
import com.nikhilsi.gitavani.viewmodel.GitaViewModel

object Routes {
    const val CHAPTER_LIST = "chapter_list"
    const val CHAPTER_DETAIL = "chapter_detail/{chapterNumber}"
    const val VERSE_DETAIL = "verse_detail/{verseId}"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val ABOUT = "about"
    const val FAVORITES = "favorites"

    fun chapterDetail(chapterNumber: Int) = "chapter_detail/$chapterNumber"
    fun verseDetail(verseId: String) = "verse_detail/$verseId"
}

@Composable
fun GitaNavGraph(
    navController: NavHostController,
    viewModel: GitaViewModel
) {
    val theme by viewModel.currentTheme.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.CHAPTER_LIST
    ) {
        composable(Routes.CHAPTER_LIST) {
            ChapterListScreen(
                dataService = viewModel.dataService,
                settings = viewModel.settings,
                readingProgress = viewModel.readingProgress,
                theme = theme,
                onChapterClick = { navController.navigate(Routes.chapterDetail(it)) },
                onVerseClick = { navController.navigate(Routes.verseDetail(it)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onHelpClick = { navController.navigate(Routes.HELP) },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(
            route = Routes.CHAPTER_DETAIL,
            arguments = listOf(navArgument("chapterNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
            ChapterDetailScreen(
                chapterNumber = chapterNumber,
                dataService = viewModel.dataService,
                settings = viewModel.settings,
                theme = theme,
                onVerseClick = { navController.navigate(Routes.verseDetail(it)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VERSE_DETAIL,
            arguments = listOf(navArgument("verseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val verseId = backStackEntry.arguments?.getString("verseId") ?: "BG1.1"
            VerseDetailScreen(
                initialVerseId = verseId,
                dataService = viewModel.dataService,
                settings = viewModel.settings,
                readingProgress = viewModel.readingProgress,
                audioService = viewModel.audioService,
                theme = theme,
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onBack = {
                    viewModel.audioService.stop()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                dataService = viewModel.dataService,
                settings = viewModel.settings,
                theme = theme,
                onVerseClick = { navController.navigate(Routes.verseDetail(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = viewModel.settings,
                theme = theme,
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                theme = theme,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpScreen(
                theme = theme,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
