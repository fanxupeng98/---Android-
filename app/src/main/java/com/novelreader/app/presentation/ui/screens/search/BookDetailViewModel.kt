package com.novelreader.app.presentation.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.data.remote.BookParser
import com.novelreader.app.domain.model.Book
import com.novelreader.app.domain.model.Chapter
import com.novelreader.app.domain.repository.BookRepository
import com.novelreader.app.domain.repository.BookSourceRepository
import com.novelreader.app.domain.repository.ChapterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingChapters: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val bookSourceRepository: BookSourceRepository,
    private val chapterRepository: ChapterRepository,
    private val bookParser: BookParser
) : ViewModel() {
    
    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L
    
    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadBook()
    }
    
    private fun loadBook() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val book = bookRepository.getBookById(bookId)
            if (book != null) {
                _uiState.value = _uiState.value.copy(book = book)
                
                // 尝试加载本地章节
                val localChapters = chapterRepository.getChaptersByBookId(bookId)
                if (localChapters.isEmpty() && book.detailUrl != null) {
                    loadChaptersFromSource(book)
                } else {
                    _uiState.value = _uiState.value.copy(
                        chapters = localChapters,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "书籍不存在",
                    isLoading = false
                )
            }
        }
    }
    
    private suspend fun loadChaptersFromSource(book: Book) {
        _uiState.value = _uiState.value.copy(isLoadingChapters = true)
        
        val source = bookSourceRepository.getSourceById(book.sourceId)
        if (source != null && book.detailUrl != null) {
            val chapters = bookParser.getChapterList(book, source)
            _uiState.value = _uiState.value.copy(
                chapters = chapters,
                isLoadingChapters = false
            )
            
            // 保存章节到本地
            chapters.forEach { chapter ->
                chapterRepository.saveChapter(chapter)
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoadingChapters = false)
        }
    }
    
    fun refreshChapters() {
        viewModelScope.launch {
            val book = _uiState.value.book ?: return@launch
            _uiState.value = _uiState.value.copy(chapters = emptyList())
            loadChaptersFromSource(book)
        }
    }
    
    fun updateReadProgress(chapterIndex: Int) {
        viewModelScope.launch {
            val book = _uiState.value.book ?: return@launch
            val progress = if (_uiState.value.chapters.isNotEmpty()) {
                chapterIndex.toFloat() / _uiState.value.chapters.size
            } else 0f
            
            bookRepository.updateBook(
                book.copy(
                    lastReadChapter = chapterIndex,
                    lastReadChapterTitle = _uiState.value.chapters.getOrNull(chapterIndex)?.title,
                    lastReadTime = System.currentTimeMillis(),
                    readProgress = progress
                )
            )
        }
    }
}
