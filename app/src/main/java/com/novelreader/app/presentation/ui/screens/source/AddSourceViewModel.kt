package com.novelreader.app.presentation.ui.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.repository.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SourceFormState(
    val name: String = "",
    val baseUrl: String = "",
    val group: String = "",
    val searchUrl: String = "",
    val searchNameRule: String = "",
    val searchAuthorRule: String = "",
    val searchDetailUrlRule: String = "",
    val bookNameRule: String = "",
    val bookAuthorRule: String = "",
    val coverUrlRule: String = "",
    val introRule: String = "",
    val chapterListRule: String = "",
    val chapterNameRule: String = "",
    val contentRule: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccess: Boolean = false
)

@HiltViewModel
class AddSourceViewModel @Inject constructor(
    private val bookSourceRepository: BookSourceRepository
) : ViewModel() {
    
    private val _formState = MutableStateFlow(SourceFormState())
    val formState: StateFlow<SourceFormState> = _formState.asStateFlow()
    
    fun updateName(value: String) {
        _formState.value = _formState.value.copy(name = value)
    }
    
    fun updateBaseUrl(value: String) {
        _formState.value = _formState.value.copy(baseUrl = value)
    }
    
    fun updateGroup(value: String) {
        _formState.value = _formState.value.copy(group = value)
    }
    
    fun updateSearchUrl(value: String) {
        _formState.value = _formState.value.copy(searchUrl = value)
    }
    
    fun updateSearchNameRule(value: String) {
        _formState.value = _formState.value.copy(searchNameRule = value)
    }
    
    fun updateSearchAuthorRule(value: String) {
        _formState.value = _formState.value.copy(searchAuthorRule = value)
    }
    
    fun updateSearchDetailUrlRule(value: String) {
        _formState.value = _formState.value.copy(searchDetailUrlRule = value)
    }
    
    fun updateBookNameRule(value: String) {
        _formState.value = _formState.value.copy(bookNameRule = value)
    }
    
    fun updateBookAuthorRule(value: String) {
        _formState.value = _formState.value.copy(bookAuthorRule = value)
    }
    
    fun updateCoverUrlRule(value: String) {
        _formState.value = _formState.value.copy(coverUrlRule = value)
    }
    
    fun updateIntroRule(value: String) {
        _formState.value = _formState.value.copy(introRule = value)
    }
    
    fun updateChapterListRule(value: String) {
        _formState.value = _formState.value.copy(chapterListRule = value)
    }
    
    fun updateChapterNameRule(value: String) {
        _formState.value = _formState.value.copy(chapterNameRule = value)
    }
    
    fun updateContentRule(value: String) {
        _formState.value = _formState.value.copy(contentRule = value)
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
                val source = BookSource(
                    name = state.name,
                    baseUrl = state.baseUrl,
                    group = state.group,
                    searchUrl = state.searchUrl,
                    searchNameRule = state.searchNameRule,
                    searchAuthorRule = state.searchAuthorRule,
                    searchDetailUrlRule = state.searchDetailUrlRule,
                    bookNameRule = state.bookNameRule,
                    bookAuthorRule = state.bookAuthorRule,
                    coverUrlRule = state.coverUrlRule,
                    introRule = state.introRule,
                    chapterListRule = state.chapterListRule,
                    chapterNameRule = state.chapterNameRule,
                    contentRule = state.contentRule
                )
                
                bookSourceRepository.addSource(source)
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
