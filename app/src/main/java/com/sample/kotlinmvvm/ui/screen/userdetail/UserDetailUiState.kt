package com.sample.kotlinmvvm.ui.screen.userdetail

import com.sample.kotlinmvvm.domain.model.User

/** 用户详情页 UI 状态 */
sealed interface UserDetailUiState {
    data object Loading : UserDetailUiState
    data class Success(val user: User) : UserDetailUiState
    data class Error(val message: String) : UserDetailUiState
}
