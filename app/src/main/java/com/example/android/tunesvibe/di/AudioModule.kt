package com.example.android.tunesvibe.di

import android.content.Context
import com.example.android.tunesvibe.data.repository.AudioRepository
import com.example.android.tunesvibe.data.repository.AudioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    fun providesRepository(@ApplicationContext context: Context) : AudioRepository {
        return AudioRepositoryImpl(context)
    }
}