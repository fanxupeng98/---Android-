package com.novelreader.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.novelreader.app.presentation.ui.screens.bookshelf.BookshelfScreen
import com.novelreader.app.presentation.ui.screens.reader.ReaderScreen
import com.novelreader.app.presentation.ui.screens.search.SearchScreen
import com.novelreader.app.presentation.ui.screens.search.BookDetailScreen
import com.novelreader.app.presentation.ui.screens.source.SourceScreen
import com.novelreader.app.presentation.ui.screens.source.AddSourceScreen
import com.novelreader.app.presentation.ui.screens.source.EditSourceScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Bookshelf.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Bookshelf.route) {
            BookshelfScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToSource = {
                    navController.navigate(Screen.BookSource.route)
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BookSource.route) {
            SourceScreen(
                onAddSource = { navController.navigate(Screen.AddSource.route) },
                onEditSource = { sourceId ->
                    navController.navigate(Screen.EditSource.createRoute(sourceId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.AddSource.route) {
            AddSourceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditSource.route,
            arguments = listOf(navArgument("sourceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getLong("sourceId") ?: 0L
            EditSourceScreen(
                sourceId = sourceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            BookDetailScreen(
                bookId = bookId,
                onChapterClick = { chapterIndex ->
                    navController.navigate(Screen.Reader.createRoute(bookId, chapterIndex))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            ReaderScreen(
                bookId = bookId,
                initialChapterIndex = chapterIndex,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChapter = { newChapterIndex ->
                    navController.navigate(Screen.Reader.createRoute(bookId, newChapterIndex)) {
                        popUpTo(Screen.Reader.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
