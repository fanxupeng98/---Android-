package com.novelreader.app.di

import com.novelreader.app.data.repository.*
import com.novelreader.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindBookSourceRepository(
        impl: BookSourceRepositoryImpl
    ): BookSourceRepository
    
    @Binds
    @Singleton
    abstract fun bindBookRepository(
        impl: BookRepositoryImpl
    ): BookRepository
    
    @Binds
    @Singleton
    abstract fun bindChapterRepository(
        impl: ChapterRepositoryImpl
    ): ChapterRepository
    
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchRepositoryImpl
    ): SearchRepository
}
