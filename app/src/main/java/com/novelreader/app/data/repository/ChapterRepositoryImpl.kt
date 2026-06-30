package com.novelreader.app.data.repository

import com.novelreader.app.data.local.dao.ChapterDao
import com.novelreader.app.data.local.entity.ChapterEntity
import com.novelreader.app.domain.model.Chapter
import com.novelreader.app.domain.repository.ChapterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterRepositoryImpl @Inject constructor(
    private val chapterDao: ChapterDao
) : ChapterRepository {
    
    override suspend fun getChaptersByBookId(bookId: Long): List<Chapter> {
        return chapterDao.getChaptersByBookId(bookId).map { it.toDomainModel() }
    }
    
    override suspend fun getChapterContent(chapterId: Long): Chapter? {
        return chapterDao.getChapterById(chapterId)?.toDomainModel()
    }
    
    override suspend fun saveChapter(chapter: Chapter): Long {
        return chapterDao.insertChapter(ChapterEntity.fromDomainModel(chapter))
    }
    
    override suspend fun updateChapterContent(chapterId: Long, content: String) {
        chapterDao.updateChapterContent(chapterId, content)
    }
    
    override suspend fun deleteChaptersByBookId(bookId: Long) {
        chapterDao.deleteChaptersByBookId(bookId)
    }
}
