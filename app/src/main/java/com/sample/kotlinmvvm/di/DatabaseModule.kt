package com.sample.kotlinmvvm.di

import android.content.Context
import androidx.room.Room
import com.sample.kotlinmvvm.data.local.AppDatabase
import com.sample.kotlinmvvm.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库 Hilt 模块
 *
 * 【面试要点】
 * 1. @ApplicationContext 由 Hilt 自动提供，无需手动传入 Context
 * 2. 数据库实例必须是 @Singleton，多实例会导致 WAL 锁冲突
 * 3. fallbackToDestructiveMigration() 仅在开发阶段使用，
 *    生产环境必须编写 Migration 保证数据不丢失
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kotlin_mvvm.db"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao =
        database.userDao()
}
