package com.novelreader.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.novelreader.app.domain.model.Book

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val author: String,
    val coverUrl: String?,
    val intro: String?,
    val detailUrl: String?,
    val sourceId: Long,
    val sourceName: String,
    val lastReadChapter: Int,
    val lastReadChapterTitle: String?,
    val lastReadTime: Long,
    val addedTime: Long,
    val readProgress: Float
) {
    fun toDomainModel() = Book(
        id = id,
        name = name,
        author = author,
        coverUrl = coverUrl,
        intro = intro,
        detailUrl = detailUrl,
        sourceId = sourceId,
        sourceName = sourceName,
        lastReadChapter = lastReadChapter,
        lastReadChapterTitle = lastReadChapterTitle,
        lastReadTime = lastReadTime,
        addedTime = addedTime,
        readProgress = readProgress
    )

    companion object {
        fun fromDomainModel(book: Book) = BookEntity(
            id = book.id,
            name = book.name,
            author = book.author,
            coverUrl = book.coverUrl,
            intro = book.intro,
            detailUrl = book.detailUrl,
            sourceId = book.sourceId,
            sourceName = book.sourceName,
            lastReadChapter = book.lastReadChapter,
            lastReadChapterTitle = book.lastReadChapterTitle,
            lastReadTime = book.lastReadTime,
            addedTime = book.addedTime,
            readProgress = book.readProgress
        )
    }
}
