package com.novelreader.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.novelreader.app.domain.model.BookSource

@Entity(tableName = "book_sources")
data class BookSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val groupName: String,
    val type: String,
    val searchUrl: String,
    val searchNameRule: String,
    val searchAuthorRule: String,
    val searchDetailUrlRule: String,
    val bookNameRule: String,
    val bookAuthorRule: String,
    val coverUrlRule: String,
    val introRule: String,
    val chapterUrlRule: String,
    val chapterListRule: String,
    val chapterNameRule: String,
    val contentRule: String,
    val enabled: Boolean,
    val customOrder: Int
) {
    fun toDomainModel() = BookSource(
        id = id,
        name = name,
        baseUrl = baseUrl,
        group = groupName,
        type = type,
        searchUrl = searchUrl,
        searchNameRule = searchNameRule,
        searchAuthorRule = searchAuthorRule,
        searchDetailUrlRule = searchDetailUrlRule,
        bookNameRule = bookNameRule,
        bookAuthorRule = bookAuthorRule,
        coverUrlRule = coverUrlRule,
        introRule = introRule,
        chapterUrlRule = chapterUrlRule,
        chapterListRule = chapterListRule,
        chapterNameRule = chapterNameRule,
        contentRule = contentRule,
        enabled = enabled,
        customOrder = customOrder
    )

    companion object {
        fun fromDomainModel(source: BookSource) = BookSourceEntity(
            id = source.id,
            name = source.name,
            baseUrl = source.baseUrl,
            groupName = source.group,
            type = source.type,
            searchUrl = source.searchUrl,
            searchNameRule = source.searchNameRule,
            searchAuthorRule = source.searchAuthorRule,
            searchDetailUrlRule = source.searchDetailUrlRule,
            bookNameRule = source.bookNameRule,
            bookAuthorRule = source.bookAuthorRule,
            coverUrlRule = source.coverUrlRule,
            introRule = source.introRule,
            chapterUrlRule = source.chapterUrlRule,
            chapterListRule = source.chapterListRule,
            chapterNameRule = source.chapterNameRule,
            contentRule = source.contentRule,
            enabled = source.enabled,
            customOrder = source.customOrder
        )
    }
}
