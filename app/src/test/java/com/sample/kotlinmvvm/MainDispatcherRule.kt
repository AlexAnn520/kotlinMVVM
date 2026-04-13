package com.sample.kotlinmvvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule：将 Dispatchers.Main 替换为 TestDispatcher
 *
 * 【面试重点：为什么测试 ViewModel 需要这个 Rule？】
 * - viewModelScope 默认使用 Dispatchers.Main
 * - 单元测试环境没有 Android Main Looper，直接调用会崩溃
 * - 此 Rule 在每个测试前将 Main 替换为 UnconfinedTestDispatcher（立即执行，不排队）
 * - 测试结束后恢复原始 Dispatcher，避免测试间互相影响
 *
 * 使用方式：在测试类中声明 @get:Rule val mainDispatcherRule = MainDispatcherRule()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
