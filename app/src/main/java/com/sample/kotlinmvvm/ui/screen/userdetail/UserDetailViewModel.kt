package com.sample.kotlinmvvm.ui.screen.userdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.kotlinmvvm.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户详情 ViewModel
 *
 * 【面试重点：SavedStateHandle 获取导航参数】
 * Navigation Compose 类型安全路由会将参数以属性名为 key 存入 SavedStateHandle。
 * 例如 Route.UserDetail(userId = 1) → savedStateHandle["userId"] = 1
 *
 * 注意：savedStateHandle.toRoute<T>() 方法内部依赖 Android Bundle，
 * 在纯 JVM 单元测试中不可用。推荐直接用 savedStateHandle["key"] 提取参数，
 * 这样 ViewModel 在 JVM 单元测试中可直接构造 SavedStateHandle 传入参数。
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userRepository.getUserById(userId)
                .catch { e ->
                    _uiState.value = UserDetailUiState.Error(
                        e.message ?: "加载用户详情失败"
                    )
                }
                .collect { user ->
                    _uiState.value = UserDetailUiState.Success(user)
                }
        }
    }
}
