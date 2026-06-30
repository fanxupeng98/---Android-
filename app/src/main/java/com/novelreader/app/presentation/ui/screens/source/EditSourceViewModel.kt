package com.novelreader.app.presentation.ui.screens.source

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.model.RuleBookInfo
import com.novelreader.app.domain.model.RuleContent
import com.novelreader.app.domain.model.RuleSearch
import com.novelreader.app.domain.model.RuleToc
import com.novelreader.app.domain.repository.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSourceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookSourceRepository: BookSourceRepository
) : ViewModel() {
    
    private val sourceId: Long = savedStateHandle.get<Long>("sourceId") ?: 0L
    
    private val _formState = MutableStateFlow(SourceFormState())
    val formState: StateFlow<SourceFormState> = _formState.asStateFlow()
    
    private var originalSource: BookSource? = null
    
    init {
        loadSource()
    }
    
    private fun loadSource() {
        viewModelScope.launch {
            val source = bookSourceRepository.getSourceById(sourceId)
            if (source != null) {
                originalSource = source
                _formState.value = SourceFormState(
                    name = source.name,
                    baseUrl = source.baseUrl,
                    group = source.group,
                    type = source.type,
                    searchUrl = source.searchUrl,
                    exploreUrl = source.exploreUrl,
                    searchBookList = source.ruleSearch?.bookList ?: "",
                    searchName = source.ruleSearch?.name ?: "",
                    searchAuthor = source.ruleSearch?.author ?: "",
                    searchBookUrl = source.ruleSearch?.bookUrl ?: "",
                    bookName = source.ruleBookInfo?.name ?: "",
                    bookAuthor = source.ruleBookInfo?.author ?: "",
                    bookCoverUrl = source.ruleBookInfo?.coverUrl ?: "",
                    bookIntro = source.ruleBookInfo?.intro ?: "",
                    tocChapterList = source.ruleToc?.chapterList ?: "",
                    tocChapterName = source.ruleToc?.chapterName ?: "",
                    tocChapterUrl = source.ruleToc?.chapterUrl ?: "",
                    content = source.ruleContent?.content ?: ""
                )
            }
        }
    }
    
    fun updateName(value: String) {
        _formState.value = _formState.value.copy(name = value)
    }
    
    fun updateBaseUrl(value: String) {
        _formState.value = _formState.value.copy(baseUrl = value)
    }
    
    fun updateGroup(value: String) {
        _formState.value = _formState.value.copy(group = value)
    }
    
    fun updateType(value: Int) {
        _formState.value = _formState.value.copy(type = value)
    }
    
    fun updateSearchUrl(value: String) {
        _formState.value = _formState.value.copy(searchUrl = value)
    }
    
    fun updateExploreUrl(value: String) {
        _formState.value = _formState.value.copy(exploreUrl = value)
    }
    
    fun updateSearchBookList(value: String) {
        _formState.value = _formState.value.copy(searchBookList = value)
    }
    
    fun updateSearchName(value: String) {
        _formState.value = _formState.value.copy(searchName = value)
    }
    
    fun updateSearchAuthor(value: String) {
        _formState.value = _formState.value.copy(searchAuthor = value)
    }
    
    fun updateSearchBookUrl(value: String) {
        _formState.value = _formState.value.copy(searchBookUrl = value)
    }
    
    fun updateBookName(value: String) {
        _formState.value = _formState.value.copy(bookName = value)
    }
    
    fun updateBookAuthor(value: String) {
        _formState.value = _formState.value.copy(bookAuthor = value)
    }
    
    fun updateBookCoverUrl(value: String) {
        _formState.value = _formState.value.copy(bookCoverUrl = value)
    }
    
    fun updateBookIntro(value: String) {
        _formState.value = _formState.value.copy(bookIntro = value)
    }
    
    fun updateTocChapterList(value: String) {
        _formState.value = _formState.value.copy(tocChapterList = value)
    }
    
    fun updateTocChapterName(value: String) {
        _formState.value = _formState.value.copy(tocChapterName = value)
    }
    
    fun updateTocChapterUrl(value: String) {
        _formState.value = _formState.value.copy(tocChapterUrl = value)
    }
    
    fun updateContent(value: String) {
        _formState.value = _formState.value.copy(content = value)
    }
    
    fun saveSource() {
        val state = _formState.value
        
        if (state.name.isBlank()) {
            _formState.value = state.copy(error = "请输入书源名称")
            return
        }
        
        if (state.baseUrl.isBlank()) {
            _formState.value = state.copy(error = "请输入书源 URL")
            return
        }
        
        viewModelScope.launch {
            _formState.value = state.copy(isSaving = true, error = null)
            
            try {
                val source = originalSource!!.copy(
                    name = state.name,
                    baseUrl = state.baseUrl,
                    group = state.group,
                    type = state.type,
                    searchUrl = state.searchUrl,
                    exploreUrl = state.exploreUrl,
                    ruleSearch = if (state.searchName.isNotBlank() || state.searchBookList.isNotBlank()) {
                        RuleSearch(
                            bookList = state.searchBookList,
                            name = state.searchName,
                            author = state.searchAuthor,
                            bookUrl = state.searchBookUrl
                        )
                    } else null,
                    ruleBookInfo = if (state.bookName.isNotBlank()) {
                        RuleBookInfo(
                            name = state.bookName,
                            author = state.bookAuthor,
                            coverUrl = state.bookCoverUrl,
                            intro = state.bookIntro
                        )
                    } else null,
                    ruleToc = if (state.tocChapterList.isNotBlank()) {
                        RuleToc(
                            chapterList = state.tocChapterList,
                            chapterName = state.tocChapterName,
                            chapterUrl = state.tocChapterUrl
                        )
                    } else null,
                    ruleContent = if (state.content.isNotBlank()) {
                        RuleContent(content = state.content)
                    } else null
                )
                
                bookSourceRepository.updateSource(source)
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    savedSuccess = true
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }
}
