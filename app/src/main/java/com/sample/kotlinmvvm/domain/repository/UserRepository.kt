package com.sample.kotlinmvvm.domain.repository

import com.sample.kotlinmvvm.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户仓库接口 — 定义数据操作契约
 *
 * 【面试要点】Repository 模式的核心：
 * 1. 接口定义在 domain 层，实现在 data 层 → 依赖倒置原则（DIP）
 * 2. 返回 Flow 实现响应式数据流，UI 自动感知数据变化
 * 3. ViewModel 依赖接口而非实现，便于用 Fake 替换进行单元测试
 */
interface UserRepository {

    /** 获取用户列表（持续观察数据库变化） */
    fun getUsers(): Flow<List<User>>

    /** 根据 ID 获取单个用户 */
    fun getUserById(id: Int): Flow<User>

    /** 从网络刷新数据并写入数据库 */
    suspend fun refreshUsers()
}
