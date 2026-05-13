package com.example.nammaraste.di

import android.content.Context
import androidx.room.Room
import com.example.nammaraste.data.local.AppDatabase
import com.example.nammaraste.data.local.ReportDao
import com.example.nammaraste.utils.Constants
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideReportDao(database: AppDatabase): ReportDao = database.reportDao()
}