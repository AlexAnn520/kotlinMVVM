package com.sample.kotlinmvvm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sample.kotlinmvvm.data.local.dao.UserDao
import com.sample.kotlinmvvm.data.local.entity.UserEntity

/**
 * Room 数据库定义
 *
 * 【面试要点】
 * 1. version = 1 表示初始版本，后续表结构变更需递增版本号并编写 Migration
 * 2. exportSchema = false 在开发阶段关闭 Schema 导出，生产环境建议开启用于版本管理
 * 3. 通过 Hilt 以单例模式提供，避免多实例引起的数据库锁问题
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
