package com.sample.kotlinmvvm.ui.screen.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.kotlinmvvm.domain.repository.UserRepository
import com.sample.kotlinmvvm.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户列表 ViewModel
 *
 * 【面试重点 1：StateFlow vs LiveData】
 * - StateFlow 是 Kotlin 原生 API，不依赖 Android 框架 → 纯 Kotlin 模块也能用
 * - 始终有值（构造时必须传初始值），不存在 LiveData 的 null 初始状态问题
 * - 支持 Flow 操作符（map、combine、filter），数据变换更灵活
 * - Compose 中用 collectAsStateWithLifecycle() 收集，自动感知生命周期
 * - 适合多模块 / KMM 项目
 *
 * 【面试重点 2：ViewModel 如何存活配置变更（如屏幕旋转）？】
 * - ViewModel 存储在 ViewModelStore 中，由 ViewModelStoreOwner（Activity/Fragment）持有
 * - 配置变更时，Activity 重建但 ViewModelStore 被保留（通过 NonConfigurationInstances 机制）
 * - viewModelScope 绑定 ViewModel 生命周期，onCleared() 时自动取消所有协程
 *
 * 【面试重点 3：协程异常处理策略】
 * - viewModelScope.launch 内部用 try/catch 捕获异常（推荐，可控性最强）
 * - Flow 用 .catch {} 操作符捕获上游异常
 * - 不推荐 CoroutineExceptionHandler：它无法恢复异常，只能做日志记录
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    // 一次性事件通道：Channel 保证事件只被消费一次
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadUsers()
    }

    /** 加载用户列表（观察数据库 Flow + 触发网络刷新） */
    fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers()
                .onStart {
                    // 仅在首次加载时显示全屏 Loading
                    if (_uiState.value !is UserListUiState.Success) {
                        _uiState.value = UserListUiState.Loading
                    }
                    // 触发网络刷新（静默失败，不阻塞本地数据展示）
                    try {
                        userRepository.refreshUsers()
                    } catch (e: Exception) {
                        // 如果本地无数据且网络失败，显示错误
                        // 如果本地有数据，静默失败（用户看到的是缓存数据）
                    }
                }
                .catch { e ->
                    _uiState.value = UserListUiState.Error(
                        e.message ?: "加载失败，请重试"
                    )
                }
                .collect { users ->
                    _uiState.value = if (users.isEmpty()) {
                        UserListUiState.Empty
                    } else {
                        UserListUiState.Success(users)
                    }
                }
        }
    }

    /** 下拉刷新 */
    fun refresh() {
        viewModelScope.launch {
            // 更新刷新状态
            val currentState = _uiState.value
            if (currentState is UserListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }

            try {
                userRepository.refreshUsers()
                // 刷新成功后，Room Flow 会自动发射新数据
            } catch (e: Exception) {
                _uiEvent.send(
                    UiEvent.ShowSnackbar("刷新失败：${e.message}")
                )
            } finally {
                val state = _uiState.value
                if (state is UserListUiState.Success) {
                    _uiState.value = state.copy(isRefreshing = false)
                }
            }
        }
    }
}
