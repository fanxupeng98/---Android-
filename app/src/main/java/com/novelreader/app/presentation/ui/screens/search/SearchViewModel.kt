package com.novelreader.app.presentation.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.model.SearchResult
import com.novelreader.app.domain.repository.BookRepository
import com.novelreader.app.domain.repository.BookSourceRepository
import com.novelreader.app.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val keyword: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val sources: List<BookSource> = emptyList(),
    val selectedSourceId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val bookRepository: BookRepository,
    private val bookSourceRepository: BookSourceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        loadSources()
    }
    
    private fun loadSources() {
        viewModelScope.launch {
            bookSourceRepository.getAllSources().collect { sources ->
                _uiState.value = _uiState.value.copy(sources = sources)
            }
        }
    }
    
    fun updateKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(keyword = keyword)
        
        // 防抖搜索
        searchJob?.cancel()
        if (keyword.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                search()
            }
        } else {
            _uiState.value = _uiState.value.copy(results = emptyList())
        }
    }
    
    fun selectSource(sourceId: Long?) {
        _uiState.value = _uiState.value.copy(selectedSourceId = sourceId)
        if (_uiState.value.keyword.length >= 2) {
            search()
        }
    }
    
    fun search() {
        val keyword = _uiState.value.keyword.trim()
        if (keyword.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            try {
                val results = searchRepository.search(
                    keyword = keyword,
                    sourceId = _uiState.value.selectedSourceId
                )
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "搜索失败",
                    isSearching = false
                )
            }
        }
    }
    
    fun addToBookshelf(result: SearchResult) {
        viewModelScope.launch {
            // 检查是否已添加
            val existing = bookRepository.getBookByDetailUrl(result.detailUrl, result.sourceId)
            if (existing != null) {
                return@launch
            }
            
            val book = Book(
                name = result.name,
                author = result.author,
                detailUrl = result.detailUrl,
                sourceId = result.sourceId,
                sourceName = result.sourceName
            )
            bookRepository.addBook(book)
        }
    }
}
