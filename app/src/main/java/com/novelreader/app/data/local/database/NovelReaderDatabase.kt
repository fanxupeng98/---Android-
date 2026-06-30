package com.novelreader.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.novelreader.app.data.local.converter.BookSourceConverters
import com.novelreader.app.data.local.dao.BookDao
import com.novelreader.app.data.local.dao.BookSourceDao
import com.novelreader.app.data.local.dao.ChapterDao
import com.novelreader.app.data.local.entity.BookEntity
import com.novelreader.app.data.local.entity.BookSourceEntity
import com.novelreader.app.data.local.entity.ChapterEntity

@Database(
    entities = [
        BookSourceEntity::class,
        BookEntity::class,
        ChapterEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(BookSourceConverters::class)
abstract class NovelReaderDatabase : RoomDatabase() {
    abstract fun bookSourceDao(): BookSourceDao
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
}
