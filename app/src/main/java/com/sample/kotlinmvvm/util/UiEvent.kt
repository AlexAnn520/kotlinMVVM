package com.sample.kotlinmvvm.util

/**
 * 一次性 UI 事件
 *
 * 【面试重点：一次性事件（One-time Event）的处理方式】
 *
 * 问题：StateFlow 有 replay，新订阅者会收到最后一个值，不适合表示"事件"。
 * 比如 Snackbar 只该显示一次，屏幕旋转后不应重复显示。
 *
 * 方案对比：
 * 1. Channel + receiveAsFlow() ← 本项目采用
 *    - 保证每个事件只被消费一次
 *    - 即使没有订阅者也会缓存事件（不丢失）
 *    - 配合 LaunchedEffect(Unit) 在 Compose 中收集
 *
 * 2. SharedFlow(replay=0)
 *    - 事件只发给当前订阅者，无订阅者时事件丢失
 *    - 适合"可丢弃"的事件场景
 *
 * 3. 包装类（Event<T>）— 已过时，不推荐
 *    - LiveData 时代的方案，Compose 中无必要
 */
sealed interface UiEvent {
    /** 显示 Snackbar 提示 */
    data class ShowSnackbar(val message: String) : UiEvent
}
