package com.novelreader.app.di

import android.content.Context
import androidx.room.Room
import com.novelreader.app.data.local.dao.BookDao
import com.novelreader.app.data.local.dao.BookSourceDao
import com.novelreader.app.data.local.dao.ChapterDao
import com.novelreader.app.data.local.database.NovelReaderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NovelReaderDatabase {
        return Room.databaseBuilder(
            context,
            NovelReaderDatabase::class.java,
            "novel_reader.db"
        ).build()
    }
    
    @Provides
    fun provideBookSourceDao(database: NovelReaderDatabase): BookSourceDao {
        return database.bookSourceDao()
    }
    
    @Provides
    fun provideBookDao(database: NovelReaderDatabase): BookDao {
        return database.bookDao()
    }
    
    @Provides
    fun provideChapterDao(database: NovelReaderDatabase): ChapterDao {
        return database.chapterDao()
    }
}
