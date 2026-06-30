package com.novelreader.app.presentation.ui.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.data.remote.HtmlFetcher
import com.novelreader.app.domain.model.BookSource
import com.novelreader.app.domain.repository.BookSourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val bookSourceRepository: BookSourceRepository,
    private val htmlFetcher: HtmlFetcher
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
    
    /**
     * 从 URL 导入书源（支持批量）
     */
    fun importFromUrl(url: String, callback: (Boolean, List<BookSource>, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    htmlFetcher.fetchJson(url)
                }
                
                result.fold(
                    onSuccess = { json ->
                        val sources = BookSource.fromJsonList(json)
                        if (sources.isNullOrEmpty()) {
                            callback(false, emptyList(), "JSON 格式无效，无法解析为书源")
                        } else {
                            callback(true, sources, null)
                        }
                    },
                    onFailure = { e ->
                        callback(false, emptyList(), "网络请求失败: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                callback(false, emptyList(), "导入失败: ${e.message}")
            }
        }
    }
    
    /**
     * 从本地 JSON 导入书源（支持批量）
     */
    fun importFromLocalJson(json: String, callback: (Boolean, List<BookSource>, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val sources = BookSource.fromJsonList(json)
                if (sources.isNullOrEmpty()) {
                    callback(false, emptyList(), "JSON 格式无效，无法解析为书源")
                } else {
                    callback(true, sources, null)
                }
            } catch (e: Exception) {
                callback(false, emptyList(), "解析失败: ${e.message}")
            }
        }
    }
    
    /**
     * 批量导入书源
     */
    fun batchImportSources(sources: List<BookSource>, callback: (Int) -> Unit) {
        viewModelScope.launch {
            var successCount = 0
            sources.forEach { source ->
                try {
                    bookSourceRepository.addSource(source.copy(id = 0, enabled = true))
                    successCount++
                } catch (e: Exception) {
                    // 单个失败不影响其他
                }
            }
            callback(successCount)
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
