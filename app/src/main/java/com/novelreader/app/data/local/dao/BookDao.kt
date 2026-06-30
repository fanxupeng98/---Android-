package com.novelreader.app.data.local.dao

import androidx.room.*
import com.novelreader.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?
    
    @Query("SELECT * FROM books WHERE detailUrl = :detailUrl AND sourceId = :sourceId")
    suspend fun getBookByDetailUrl(detailUrl: String, sourceId: Long): BookEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long
    
    @Update
    suspend fun updateBook(book: BookEntity)
    
    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Long)
}
