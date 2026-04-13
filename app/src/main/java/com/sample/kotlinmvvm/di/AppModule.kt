package com.sample.kotlinmvvm.di

import com.sample.kotlinmvvm.data.repository.UserRepositoryImpl
import com.sample.kotlinmvvm.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * IO 调度器限定符
 *
 * 【面试要点】使用自定义 Qualifier 注入 Dispatcher 而非直接使用 Dispatchers.IO：
 * 单元测试时可替换为 TestDispatcher，避免真实线程切换导致测试不稳定。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * 应用级 Hilt 模块 — 提供全局单例依赖
 *
 * 【面试要点】Hilt Module 的作用：
 * 1. @InstallIn(SingletonComponent::class) 表示模块中的依赖与 Application 同生命周期
 * 2. @Provides 用于提供第三方库实例（无法直接加 @Inject 的类）
 * 3. @Binds 用于绑定接口与实现（比 @Provides 更高效，无需创建新实例）
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

/** 使用 @Binds 绑定 Repository 接口与实现 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
