package com.novelreader.app.data.local.dao

import androidx.room.*
import com.novelreader.app.data.local.entity.BookSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookSourceDao {
    @Query("SELECT * FROM book_sources ORDER BY customOrder ASC, id ASC")
    fun getAllSources(): Flow<List<BookSourceEntity>>
    
    @Query("SELECT * FROM book_sources WHERE enabled = 1 ORDER BY customOrder ASC")
    suspend fun getEnabledSources(): List<BookSourceEntity>
    
    @Query("SELECT * FROM book_sources WHERE id = :id")
    suspend fun getSourceById(id: Long): BookSourceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: BookSourceEntity): Long
    
    @Update
    suspend fun updateSource(source: BookSourceEntity)
    
    @Query("DELETE FROM book_sources WHERE id = :id")
    suspend fun deleteSource(id: Long)
    
    @Query("UPDATE book_sources SET enabled = :enabled WHERE id = :id")
    suspend fun toggleSourceEnabled(id: Long, enabled: Boolean)
}
