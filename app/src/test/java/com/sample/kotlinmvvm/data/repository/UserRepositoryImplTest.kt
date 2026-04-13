package com.sample.kotlinmvvm.data.repository

import com.sample.kotlinmvvm.data.local.dao.UserDao
import com.sample.kotlinmvvm.data.local.entity.UserEntity
import com.sample.kotlinmvvm.data.remote.api.UserApi
import com.sample.kotlinmvvm.data.remote.dto.AddressDto
import com.sample.kotlinmvvm.data.remote.dto.CompanyDto
import com.sample.kotlinmvvm.data.remote.dto.UserDto
import com.sample.kotlinmvvm.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Repository 单元测试 — 使用 MockK
 *
 * 【面试要点】Repository 测试重点：
 * 1. 验证数据流向：API → DAO（网络数据正确写入数据库）
 * 2. 验证数据转换：Entity → Domain Model（Mapper 逻辑正确）
 * 3. 验证离线场景：API 失败时，数据库数据仍可正常流出
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val mockApi = mockk<UserApi>()
    private val mockDao = mockk<UserDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: UserRepositoryImpl

    private val testEntity = UserEntity(
        id = 1, name = "张三", username = "zhangsan",
        email = "zhangsan@example.com", phone = "123",
        website = "example.com", company = "测试公司", address = "北京, 长安街"
    )

    private val testDto = UserDto(
        id = 1, name = "张三", username = "zhangsan",
        email = "zhangsan@example.com", phone = "123",
        website = "example.com",
        company = CompanyDto(name = "测试公司"),
        address = AddressDto(street = "长安街", city = "北京")
    )

    @Before
    fun setup() {
        repository = UserRepositoryImpl(mockApi, mockDao, testDispatcher)
    }

    @Test
    fun `getUsers 返回数据库数据映射为领域模型`() = runTest {
        every { mockDao.getAllUsers() } returns flowOf(listOf(testEntity))

        val result = repository.getUsers().first()

        assertEquals(1, result.size)
        assertEquals("张三", result[0].name)
        assertEquals("zhangsan", result[0].username)
    }

    @Test
    fun `refreshUsers 从 API 获取数据并写入数据库`() = runTest {
        coEvery { mockApi.getUsers() } returns listOf(testDto)

        repository.refreshUsers()

        // 验证 API 被调用
        coVerify { mockApi.getUsers() }
        // 验证数据写入数据库
        coVerify { mockDao.deleteAllAndInsert(any()) }
    }

    @Test(expected = RuntimeException::class)
    fun `refreshUsers 网络异常时向上传播`() = runTest {
        coEvery { mockApi.getUsers() } throws RuntimeException("网络异常")

        repository.refreshUsers()
    }
}
