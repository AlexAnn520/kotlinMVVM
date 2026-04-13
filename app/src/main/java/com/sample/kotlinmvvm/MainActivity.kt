package com.sample.kotlinmvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sample.kotlinmvvm.ui.navigation.AppNavigation
import com.sample.kotlinmvvm.ui.theme.KotlinMvvmTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 单 Activity 架构入口
 *
 * 【面试要点】
 * 1. @AndroidEntryPoint 使 Hilt 能向此 Activity 及其内部的 Composable 注入依赖
 * 2. 在 Compose 中，Activity 仅作为容器，所有 UI 和导航逻辑在 Composable 中处理
 * 3. enableEdgeToEdge() 启用全屏沉浸式布局，状态栏和导航栏背后也可绘制内容
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinMvvmTheme {
                AppNavigation()
            }
        }
    }
}
