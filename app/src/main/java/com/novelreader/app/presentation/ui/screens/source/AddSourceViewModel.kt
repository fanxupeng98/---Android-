package com.novelreader.app.presentation.ui.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novelreader.app.data.remote.HtmlFetcher
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

/** 导入来源类型 */
sealed class ImportSource {
    data class Url(val url: String) : ImportSource()
    data class LocalJson(val json: String) : ImportSource()
}

/** 导入结果 */
data class ImportResult(
    val sources: List<BookSource> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val showImportDialog: Boolean = false,
    val importUrl: String = "",
    val parseSuccess: Boolean = false  // 解析成功，等待用户确认
)

@HiltViewModel
class AddSourceViewModel @Inject constructor(
    private val bookSourceRepository: BookSourceRepository,
    private val htmlFetcher: HtmlFetcher
) : ViewModel() {
    
    private val _formState = MutableStateFlow(SourceFormState())
    val formState: StateFlow<SourceFormState> = _formState.asStateFlow()

    private val _importResult = MutableStateFlow(ImportResult())
    val importResult: StateFlow<ImportResult> = _importResult.asStateFlow()

    private val gson = Gson()

    // ========== 表单更新 ==========

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

    // ========== 导入相关 ==========

    fun showImportDialog() {
        _importResult.value = ImportResult(showImportDialog = true)
    }

    fun hideImportDialog() {
        _importResult.value = ImportResult(showImportDialog = false)
    }

    fun updateImportUrl(url: String) {
        _importResult.value = _importResult.value.copy(importUrl = url)
    }

    /**
     * 从 URL 导入书源
     */
    fun importFromUrl(url: String) {
        if (url.isBlank()) {
            _importResult.value = _importResult.value.copy(error = "请输入书源 URL")
            return
        }

        viewModelScope.launch {
            _importResult.value = _importResult.value.copy(isLoading = true, error = null)

            htmlFetcher.fetchJson(url).fold(
                onSuccess = { json ->
                    parseAndShowSources(json, url)
                },
                onFailure = { e ->
                    _importResult.value = _importResult.value.copy(
                        isLoading = false,
                        error = "获取失败: ${e.message}"
                    )
                }
            )
        }
    }

    /**
     * 从本地 JSON 文件导入
     */
    fun importFromLocalJson(json: String) {
        if (json.isBlank()) {
            _importResult.value = _importResult.value.copy(error = "文件内容为空")
            return
        }

        viewModelScope.launch {
            _importResult.value = _importResult.value.copy(isLoading = true, error = null)
            parseAndShowSources(json, null)
        }
    }

    /**
     * 解析 JSON 并展示结果（支持单个和数组）
     */
    private suspend fun parseAndShowSources(json: String, sourceUrl: String?) {
        val trimmed = json.trim()
        val sources = try {
            when {
                // 数组格式 [...] 
                trimmed.startsWith("[") -> {
                    val type = object : TypeToken<List<BookSource>>() {}.type
                    gson.fromJson<List<BookSource>>(trimmed, type)
                }
                // 单个对象格式 {...}
                trimmed.startsWith("{") -> {
                    val source = gson.fromJson(trimmed, BookSource::class.java)
                    if (source != null && source.name.isNotBlank()) {
                        listOf(source)
                    } else {
                        null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }

        if (sources.isNullOrEmpty()) {
            _importResult.value = _importResult.value.copy(
                isLoading = false,
                error = "JSON 格式无效，无法解析为书源"
            )
        } else {
            _importResult.value = _importResult.value.copy(
                isLoading = false,
                sources = sources,
                parseSuccess = true,
                importUrl = sourceUrl ?: _importResult.value.importUrl,
                error = null
            )
        }
    }

    /**
     * 从导入结果中选择并应用某个书源到表单
     */
    fun applySource(source: BookSource) {
        _formState.value = SourceFormState(
            name = source.name,
            baseUrl = source.baseUrl,
            group = source.group,
            searchUrl = source.searchUrl,
            searchNameRule = source.searchNameRule,
            searchAuthorRule = source.searchAuthorRule,
            searchDetailUrlRule = source.searchDetailUrlRule,
            bookNameRule = source.bookNameRule,
            bookAuthorRule = source.bookAuthorRule,
            coverUrlRule = source.coverUrlRule,
            introRule = source.introRule,
            chapterListRule = source.chapterListRule,
            chapterNameRule = source.chapterNameRule,
            contentRule = source.contentRule
        )
        _importResult.value = ImportResult(showImportDialog = false)
    }

    /**
     * 保存选中的书源（从导入结果批量保存）
     */
    fun saveImportedSource(source: BookSource) {
        viewModelScope.launch {
            try {
                bookSourceRepository.addSource(source.copy(id = 0, enabled = true))
            } catch (e: Exception) {
                _importResult.value = _importResult.value.copy(
                    error = "保存失败: ${e.message}"
                )
            }
        }
    }

    // ========== 保存 ==========

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
