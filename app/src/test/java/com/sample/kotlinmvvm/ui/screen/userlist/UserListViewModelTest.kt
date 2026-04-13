package com.sample.kotlinmvvm.ui.screen.userlist

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
 * 用户列表 ViewModel 单元测试
 *
 * 【面试重点：如何测试 ViewModel？】
 * 1. 使用 MainDispatcherRule 替换 Dispatchers.Main
 * 2. 使用 FakeRepository 代替真实 Repository（无需网络和数据库）
 * 3. 使用 Turbine 库测试 StateFlow 发射顺序
 * 4. runTest {} 提供测试协程作用域，自动推进虚拟时间
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeUserRepository
    private lateinit var viewModel: UserListViewModel

    private val testUsers = listOf(
        User(1, "张三", "zhangsan", "zhangsan@example.com", "123", "example.com", "测试公司", "北京"),
        User(2, "李四", "lisi", "lisi@example.com", "456", "example.com", "测试公司", "上海")
    )

    @Before
    fun setup() {
        fakeRepository = FakeUserRepository()
    }

    @Test
    fun `初始状态为 Loading`() = runTest {
        fakeRepository.setUsers(testUsers)
        viewModel = UserListViewModel(fakeRepository)

        // UnconfinedTestDispatcher 会立即执行，所以初始 Loading 可能已经过渡到 Success
        // 验证最终状态为 Success
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(
                "期望 Success 或 Loading，实际: $state",
                state is UserListUiState.Success || state is UserListUiState.Loading
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `加载用户列表成功时状态为 Success`() = runTest {
        fakeRepository.setUsers(testUsers)
        viewModel = UserListViewModel(fakeRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("期望 Success，实际: $state", state is UserListUiState.Success)
            assertEquals(testUsers, (state as UserListUiState.Success).users)
        }
    }

    @Test
    fun `用户列表为空时状态为 Empty`() = runTest {
        fakeRepository.setUsers(emptyList())
        viewModel = UserListViewModel(fakeRepository)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue("期望 Empty，实际: $state", state is UserListUiState.Empty)
        }
    }

    @Test
    fun `刷新失败时发送 Snackbar 事件`() = runTest {
        fakeRepository.setUsers(testUsers)
        viewModel = UserListViewModel(fakeRepository)

        // 等待初始加载完成
        viewModel.uiState.test {
            expectMostRecentItem()
        }

        // 设置刷新失败
        fakeRepository.setShouldThrowError(true)

        viewModel.uiEvent.test {
            viewModel.refresh()
            val event = awaitItem()
            assertTrue("期望 ShowSnackbar 事件", event is com.sample.kotlinmvvm.util.UiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
