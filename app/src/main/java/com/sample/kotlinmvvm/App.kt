package com.sample.kotlinmvvm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口，使用 @HiltAndroidApp 触发 Hilt 代码生成
 *
 * 【面试要点】Hilt 需要一个 @HiltAndroidApp 注解的 Application 类作为依赖注入的根容器，
 * 它会自动生成 Hilt 组件并绑定到 Application 的生命周期。
 */
@HiltAndroidApp
class App : Application()
