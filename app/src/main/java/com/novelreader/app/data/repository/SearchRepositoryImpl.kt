package com.novelreader.app.data.repository

import com.novelreader.app.data.remote.BookParser
import com.novelreader.app.domain.model.SearchResult
import com.novelreader.app.domain.repository.BookRepository
import com.novelreader.app.domain.repository.BookSourceRepository
import com.novelreader.app.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val bookSourceRepository: BookSourceRepository,
    private val bookParser: BookParser
) : SearchRepository {
    
    override suspend fun search(keyword: String, sourceId: Long?): List<SearchResult> {
        val sources = if (sourceId != null) {
            listOfNotNull(bookSourceRepository.getSourceById(sourceId))
        } else {
            bookSourceRepository.getEnabledSources()
        }
        
        val allResults = mutableListOf<SearchResult>()
        
        for (source in sources) {
            try {
                val results = bookParser.search(keyword, source)
                allResults.addAll(results)
            } catch (e: Exception) {
                // 单个源失败不影响其他源
            }
        }
        
        return allResults
    }
}
