package com.sample.kotlinmvvm.ui.screen.userlist

import com.sample.kotlinmvvm.domain.model.User

/**
 * 用户列表页 UI 状态
 *
 * 【面试重点：为什么用 sealed interface 而非 sealed class？】
 * 1. sealed interface 允许实现多个密封层级，sealed class 不行
 * 2. data object 替代 object，自动生成 toString()，调试更方便
 * 3. 编译器强制 when 表达式覆盖所有分支，遗漏状态会编译报错
 * 4. 比多个 Boolean 标志位（isLoading、isError）更安全，不会出现"既 loading 又 error"的非法状态
 */
sealed interface UserListUiState {
    /** 首次加载中 */
    data object Loading : UserListUiState

    /** 加载成功 */
    data class Success(
        val users: List<User>,
        val isRefreshing: Boolean = false
    ) : UserListUiState

    /** 加载失败 */
    data class Error(val message: String) : UserListUiState

    /** 数据为空 */
    data object Empty : UserListUiState
}
