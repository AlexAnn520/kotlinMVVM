package com.sample.kotlinmvvm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sample.kotlinmvvm.ui.screen.userdetail.UserDetailScreen
import com.sample.kotlinmvvm.ui.screen.userlist.UserListScreen

/**
 * 应用导航图
 *
 * 【面试重点：Compose Navigation 最佳实践】
 * 1. 单 Activity + NavHost 架构，所有页面都是 Composable
 * 2. Screen 不直接操作 NavController，通过 lambda 回调解耦
 *    → Screen 更容易复用和测试（不依赖导航框架）
 * 3. NavController 仅在 Navigation 层持有，Screen 层对导航无感知
 * 4. 使用 composable<Route.XXX> 泛型方式注册路由（类型安全）
 */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Route.UserList
    ) {
        composable<Route.UserList> {
            UserListScreen(
                onUserClick = { userId ->
                    navController.navigate(Route.UserDetail(userId))
                }
            )
        }

        composable<Route.UserDetail> {
            UserDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
