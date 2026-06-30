package com.novelreader.app.domain.model

/**
 * 小说章节模型
 */
data class Chapter(
    val id: Long = 0,
    val bookId: Long = 0,
    val index: Int,
    val title: String,
    val url: String,
    val content: String? = null,
    val isCached: Boolean = false
)

/**
 * 搜索结果模型
 */
data class SearchResult(
    val name: String,
    val author: String,
    val detailUrl: String,
    val sourceName: String,
    val sourceId: Long
)
