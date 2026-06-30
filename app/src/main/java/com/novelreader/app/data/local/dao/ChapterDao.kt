package com.novelreader.app.data.local.dao

import androidx.room.*
import com.novelreader.app.data.local.entity.ChapterEntity

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index` ASC")
    suspend fun getChaptersByBookId(bookId: Long): List<ChapterEntity>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)
    
    @Query("UPDATE chapters SET content = :content, isCached = 1 WHERE id = :id")
    suspend fun updateChapterContent(id: Long, content: String)
    
    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: Long)
}
