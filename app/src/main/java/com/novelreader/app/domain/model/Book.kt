package com.novelreader.app.domain.model

/**
 * 小说书籍模型
 */
data class Book(
    val id: Long = 0,
    val name: String,
    val author: String,
    val coverUrl: String? = null,
    val intro: String? = null,
    val detailUrl: String? = null,
    val sourceId: Long = 0,
    val sourceName: String = "",
    val lastReadChapter: Int = 0,
    val lastReadChapterTitle: String? = null,
    val lastReadTime: Long = System.currentTimeMillis(),
    val addedTime: Long = System.currentTimeMillis(),
    val readProgress: Float = 0f // 0-1
)
