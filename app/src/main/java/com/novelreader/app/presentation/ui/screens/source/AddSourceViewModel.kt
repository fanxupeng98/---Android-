package com.novelreader.app.presentation.ui.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.app.data.remote.HtmlFetcher
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

/**
 * 简化版书源表单状态 - 仅支持基础字段和规则字符串
 */
data class SourceFormState(
    val name: String = "",
    val baseUrl: String = "",
    val group: String = "",
    val type: Int = 0,
    val searchUrl: String = "",
    val exploreUrl: String = "",
    
    // 搜索规则
    val searchBookList: String = "",
    val searchName: String = "",
    val searchAuthor: String = "",
    val searchBookUrl: String = "",
    
    // 详情规则
    val bookName: String = "",
    val bookAuthor: String = "",
    val bookCoverUrl: String = "",
    val bookIntro: String = "",
    
    // 目录规则
    val tocChapterList: String = "",
    val tocChapterName: String = "",
    val tocChapterUrl: String = "",
    
    // 正文规则
    val content: String = "",
    
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccess: Boolean = false
)

/** 导入结果 */
data class ImportResult(
    val sources: List<BookSource> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val showImportDialog: Boolean = false,
    val importUrl: String = "",
    val parseSuccess: Boolean = false
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
     * 解析 JSON 并展示结果
     */
    private suspend fun parseAndShowSources(json: String, sourceUrl: String?) {
        val sources = BookSource.fromJsonList(json)

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
