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

data class SourceUiState(
    val sources: List<BookSource> = emptyList(),
    val isLoading: Boolean = false,
    val importText: String = "",
    val importError: String? = null,
    val importSuccess: String? = null,
    val exportText: String? = null
)

@HiltViewModel
class SourceViewModel @Inject constructor(
    private val bookSourceRepository: BookSourceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SourceUiState())
    val uiState: StateFlow<SourceUiState> = _uiState.asStateFlow()
    
    init {
        loadSources()
    }
    
    private fun loadSources() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bookSourceRepository.getAllSources().collect { sources ->
                _uiState.value = _uiState.value.copy(
                    sources = sources,
                    isLoading = false
                )
            }
        }
    }
    
    fun updateImportText(text: String) {
        _uiState.value = _uiState.value.copy(importText = text)
    }
    
    fun importSource() {
        val text = _uiState.value.importText.trim()
        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(importError = "请输入书源")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(importError = null, importSuccess = null)
            
            bookSourceRepository.importSource(text)
                .onSuccess { source ->
                    _uiState.value = _uiState.value.copy(
                        importText = "",
                        importSuccess = "成功导入: ${source.name}"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        importError = "导入失败: ${e.message}"
                    )
                }
        }
    }
    
    fun exportSource(sourceId: Long) {
        viewModelScope.launch {
            val encoded = bookSourceRepository.exportSource(sourceId)
            if (encoded != null) {
                _uiState.value = _uiState.value.copy(exportText = encoded)
            }
        }
    }
    
    fun clearExportText() {
        _uiState.value = _uiState.value.copy(exportText = null)
    }
    
    fun toggleSource(sourceId: Long, enabled: Boolean) {
        viewModelScope.launch {
            bookSourceRepository.toggleSourceEnabled(sourceId, enabled)
        }
    }
    
    fun deleteSource(sourceId: Long) {
        viewModelScope.launch {
            bookSourceRepository.deleteSource(sourceId)
        }
    }
}
