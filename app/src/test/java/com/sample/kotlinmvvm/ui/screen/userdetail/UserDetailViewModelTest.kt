package com.sample.kotlinmvvm.ui.screen.userdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sample.kotlinmvvm.MainDispatcherRule
import com.sample.kotlinmvvm.data.repository.FakeUserRepository
import com.sample.kotlinmvvm.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * 用户详情 ViewModel 单元测试
 *
 * 【面试要点】
 * - SavedStateHandle 可以在测试中直接构造并传入导航参数
 * - 无需真实的导航框架，纯 JVM 环境即可运行
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeUserRepository

    private val testUser = User(
        1, "张三", "zhangsan", "zhangsan@example.com",
        "123", "example.com", "测试公司", "北京"
    )

    @Before
    fun setup() {
        fakeRepository = FakeUserRepository()
        fakeRepository.setUsers(listOf(testUser))
    }

    @Test
    fun `加载用户详情成功`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("userId" to 1))
        val viewModel = UserDetailViewModel(savedStateHandle, fakeRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("期望 Success，实际: $state", state is UserDetailUiState.Success)
            assertEquals(testUser, (state as UserDetailUiState.Success).user)
        }
    }

    @Test
    fun `用户 ID 不存在时显示错误`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("userId" to 999))

        val viewModel = UserDetailViewModel(savedStateHandle, fakeRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("期望 Error，实际: $state", state is UserDetailUiState.Error)
        }
    }
}
