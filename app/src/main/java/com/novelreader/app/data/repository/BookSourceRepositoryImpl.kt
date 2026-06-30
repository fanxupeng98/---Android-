package com.novelreader.app.data.repository

import android.util.Base64
import com.novelreader.app.data.local.dao.BookSourceDao
import com.novelreader.app.data.local.entity.BookSourceEntity
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.repository.BookSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSourceRepositoryImpl @Inject constructor(
    private val bookSourceDao: BookSourceDao
) : BookSourceRepository {
    
    override fun getAllSources(): Flow<List<BookSource>> {
        return bookSourceDao.getAllSources().map { list ->
            list.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getEnabledSources(): List<BookSource> {
        return bookSourceDao.getEnabledSources().map { it.toDomainModel() }
    }
    
    override suspend fun getSourceById(id: Long): BookSource? {
        return bookSourceDao.getSourceById(id)?.toDomainModel()
    }
    
    override suspend fun addSource(source: BookSource): Long {
        return bookSourceDao.insertSource(BookSourceEntity.fromDomainModel(source))
    }
    
    override suspend fun updateSource(source: BookSource) {
        bookSourceDao.updateSource(BookSourceEntity.fromDomainModel(source))
    }
    
    override suspend fun deleteSource(id: Long) {
        bookSourceDao.deleteSource(id)
    }
    
    override suspend fun toggleSourceEnabled(id: Long, enabled: Boolean) {
        bookSourceDao.toggleSourceEnabled(id, enabled)
    }
    
    override suspend fun importSource(base64String: String): Result<BookSource> {
        return runCatching {
            val json = String(Base64.decode(base64String.trim(), Base64.DEFAULT))
            val source = com.google.gson.Gson().fromJson(json, BookSource::class.java)
                ?: throw IllegalArgumentException("无效的书源格式")
            
            val id = addSource(source.copy(id = 0))
            source.copy(id = id)
        }
    }
    
    override suspend fun exportSource(id: Long): String? {
        val source = getSourceById(id) ?: return null
        val json = com.google.gson.Gson().toJson(source)
        return Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
    }
}
