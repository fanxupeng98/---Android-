package com.novelreader.app.domain.repository

import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.model.Chapter
import com.novelreader.app.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface BookSourceRepository {
    fun getAllSources(): Flow<List<BookSource>>
    suspend fun getEnabledSources(): List<BookSource>
    suspend fun getSourceById(id: Long): BookSource?
    suspend fun addSource(source: BookSource): Long
    suspend fun updateSource(source: BookSource)
    suspend fun deleteSource(id: Long)
    suspend fun toggleSourceEnabled(id: Long, enabled: Boolean)
    suspend fun importSource(base64String: String): Result<BookSource>
    suspend fun exportSource(id: Long): String?
}

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(id: Long)
    suspend fun getBookByDetailUrl(detailUrl: String, sourceId: Long): Book?
}

interface ChapterRepository {
    suspend fun getChaptersByBookId(bookId: Long): List<Chapter>
    suspend fun getChapterContent(chapterId: Long): Chapter?
    suspend fun saveChapter(chapter: Chapter): Long
    suspend fun updateChapterContent(chapterId: Long, content: String)
    suspend fun deleteChaptersByBookId(bookId: Long)
}

interface SearchRepository {
    suspend fun search(keyword: String, sourceId: Long? = null): List<SearchResult>
}
