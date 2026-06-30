package com.novelreader.app.data.repository

import com.novelreader.app.data.local.dao.BookDao
import com.novelreader.app.data.local.entity.BookEntity
import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {
    
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { list ->
            list.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)?.toDomainModel()
    }
    
    override suspend fun addBook(book: Book): Long {
        return bookDao.insertBook(BookEntity.fromDomainModel(book))
    }
    
    override suspend fun updateBook(book: Book) {
        bookDao.updateBook(BookEntity.fromDomainModel(book))
    }
    
    override suspend fun deleteBook(id: Long) {
        bookDao.deleteBook(id)
    }
    
    override suspend fun getBookByDetailUrl(detailUrl: String, sourceId: Long): Book? {
        return bookDao.getBookByDetailUrl(detailUrl, sourceId)?.toDomainModel()
    }
}
