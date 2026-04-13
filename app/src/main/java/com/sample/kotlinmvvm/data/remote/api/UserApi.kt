package com.sample.kotlinmvvm.data.remote.api

import com.sample.kotlinmvvm.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API 接口 — 定义网络请求
 *
 * 【面试要点】
 * 1. Retrofit 接口方法用 suspend 修饰，直接支持协程调用
 * 2. 返回值是 DTO 而非领域模型，保持网络层独立
 * 3. 配合 kotlinx-serialization Converter，实现编译时安全的 JSON 解析
 */
interface UserApi {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto
}
