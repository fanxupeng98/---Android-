package com.novelreader.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.novelreader.app.domain.model.Chapter

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val index: Int,
    val title: String,
    val url: String,
    val content: String?,
    val isCached: Boolean
) {
    fun toDomainModel() = Chapter(
        id = id,
        bookId = bookId,
        index = index,
        title = title,
        url = url,
        content = content,
        isCached = isCached
    )

    companion object {
        fun fromDomainModel(chapter: Chapter) = ChapterEntity(
            id = chapter.id,
            bookId = chapter.bookId,
            index = chapter.index,
            title = chapter.title,
            url = chapter.url,
            content = chapter.content,
            isCached = chapter.isCached
        )
    }
}
