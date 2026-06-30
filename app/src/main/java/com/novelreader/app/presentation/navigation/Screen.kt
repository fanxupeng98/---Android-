package com.novelreader.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Bookshelf : Screen("bookshelf")
    data object Search : Screen("search")
    data object BookSource : Screen("source")
    data object Reader : Screen("reader/{bookId}/{chapterIndex}") {
        fun createRoute(bookId: Long, chapterIndex: Int) = "reader/$bookId/$chapterIndex"
    }
    data object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    data object AddSource : Screen("add_source")
    data object EditSource : Screen("edit_source/{sourceId}") {
        fun createRoute(sourceId: Long) = "edit_source/$sourceId"
    }
}
