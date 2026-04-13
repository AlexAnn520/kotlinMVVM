package com.sample.kotlinmvvm.data.repository

import com.sample.kotlinmvvm.domain.model.User
import com.sample.kotlinmvvm.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 假的 Repository 实现 — 用于 ViewModel 单元测试
 *
 * 【面试重点：Fake vs Mock】
 * - Fake：手写的简化实现，有真实行为（推荐用于 Repository 测试替身）
 *   优点：可读性强、不依赖 Mock 框架、测试更稳定
 * - Mock：由 MockK/Mockito 动态生成，验证方法调用
 *   适合：验证交互行为（某方法是否被调用、调用次数）
 *
 * Google 官方推荐：优先使用 Fake，只在 Fake 实现成本过高时使用 Mock
 */
class FakeUserRepository : UserRepository {

    private val usersFlow = MutableStateFlow<List<User>>(emptyList())
    private var shouldThrowError = false

    /** 设置假数据 */
    fun setUsers(users: List<User>) {
        usersFlow.value = users
    }

    /** 设置是否抛出异常 */
    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override fun getUsers(): Flow<List<User>> {
        if (shouldThrowError) {
            throw RuntimeException("测试异常：获取用户列表失败")
        }
        return usersFlow
    }

    override fun getUserById(id: Int): Flow<User> {
        if (shouldThrowError) {
            throw RuntimeException("测试异常：获取用户详情失败")
        }
        return usersFlow.map { users ->
            users.first { it.id == id }
        }
    }

    override suspend fun refreshUsers() {
        if (shouldThrowError) {
            throw RuntimeException("测试异常：刷新失败")
        }
    }
}
