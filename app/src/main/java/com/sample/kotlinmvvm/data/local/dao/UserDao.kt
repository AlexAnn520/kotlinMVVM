package com.sample.kotlinmvvm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sample.kotlinmvvm.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO — 数据库访问对象
 *
 * 【面试要点】
 * 1. 返回 Flow<List<UserEntity>> 的查询会自动监听表变化，数据更新时重新发射
 * 2. suspend 函数用于写操作（insert/delete），自动在后台线程执行
 * 3. @Transaction 确保"清空+插入"操作的原子性，避免中间状态
 */
@Dao
interface UserDao {

    /** 观察所有用户，表数据变化时自动通知 */
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /** 根据 ID 观察单个用户 */
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    /** 先清空再插入，保证数据库与网络数据同步 */
    @Transaction
    suspend fun deleteAllAndInsert(users: List<UserEntity>) {
        deleteAll()
        insertAll(users)
    }
}
