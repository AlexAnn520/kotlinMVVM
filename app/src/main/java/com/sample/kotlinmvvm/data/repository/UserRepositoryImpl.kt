package com.sample.kotlinmvvm.data.repository

import com.sample.kotlinmvvm.data.local.dao.UserDao
import com.sample.kotlinmvvm.data.mapper.toDomain
import com.sample.kotlinmvvm.data.mapper.toEntity
import com.sample.kotlinmvvm.data.remote.api.UserApi
import com.sample.kotlinmvvm.di.IoDispatcher
import com.sample.kotlinmvvm.domain.model.User
import com.sample.kotlinmvvm.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户仓库实现 — 离线优先策略
 *
 * 【面试重点：单一数据源（Single Source of Truth）】
 * 核心思路：Room 数据库是唯一的"真相源"
 * - UI 层只观察数据库 Flow，不直接消费网络数据
 * - 网络请求获取的数据先写入数据库，再由 Flow 自动通知 UI 更新
 * - 好处：保证数据一致性、天然支持离线、避免内存中多份数据不同步
 *
 * 数据流向：Network API → Room DB → Flow → ViewModel → Compose UI
 *
 * 【面试重点：为什么注入 CoroutineDispatcher？】
 * 将 Dispatcher 作为依赖注入，而非硬编码 Dispatchers.IO：
 * 1. 单元测试时可替换为 TestDispatcher，避免测试超时
 * 2. 符合依赖倒置原则，提高代码可测试性
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getUserById(id: Int): Flow<User> {
        return userDao.getUserById(id)
            .map { it.toDomain() }
            .flowOn(ioDispatcher)
    }

    /**
     * 从网络刷新数据并同步到本地数据库
     *
     * 【面试要点】这里不需要 try/catch，异常会向上传播到 ViewModel 层处理。
     * Repository 层只负责数据操作，异常处理策略由调用方决定。
     */
    override suspend fun refreshUsers() {
        withContext(ioDispatcher) {
            val remoteUsers = userApi.getUsers()
            userDao.deleteAllAndInsert(remoteUsers.map { it.toEntity() })
        }
    }
}
