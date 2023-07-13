package com.gopal.runningappstats.di

import android.content.Context
import androidx.room.Room
import com.gopal.runningappstats.db.RunningDatabase
import com.gopal.runningappstats.utils.Constant.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDB(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()
}