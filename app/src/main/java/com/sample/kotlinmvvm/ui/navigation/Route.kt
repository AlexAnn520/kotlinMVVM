package com.sample.kotlinmvvm.ui.navigation

import kotlinx.serialization.Serializable

/**
 * 类型安全的导航路由定义
 *
 * 【面试重点：Navigation Compose 类型安全路由（2.8+）】
 * - 传统方式：navController.navigate("user_detail/$userId") → 字符串拼接，易出错
 * - 类型安全方式：navController.navigate(Route.UserDetail(userId)) → 编译时校验
 *
 * 原理：@Serializable 注解让 Navigation 在编译时生成路由的序列化/反序列化代码，
 * ViewModel 中通过 savedStateHandle.toRoute<T>() 提取参数，完全类型安全。
 */
sealed interface Route {

    /** 用户列表页 */
    @Serializable
    data object UserList : Route

    /** 用户详情页，携带 userId 参数 */
    @Serializable
    data class UserDetail(val userId: Int) : Route
}
