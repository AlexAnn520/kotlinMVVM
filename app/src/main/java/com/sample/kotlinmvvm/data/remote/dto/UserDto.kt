package com.sample.kotlinmvvm.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 网络响应数据传输对象（DTO）
 *
 * 【面试要点】DTO 与领域模型分离的好处：
 * 1. API 字段变化不会影响 UI 层
 * 2. @Serializable 是编译时注解，比 Gson 的运行时反射更安全高效
 * 3. ignoreUnknownKeys = true 配合使用，后端新增字段不会导致解析崩溃
 */
@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val company: CompanyDto,
    val address: AddressDto
)

@Serializable
data class CompanyDto(
    val name: String,
    val catchPhrase: String = "",
    val bs: String = ""
)

@Serializable
data class AddressDto(
    val street: String,
    val suite: String = "",
    val city: String,
    val zipcode: String = "",
    val geo: GeoDto = GeoDto()
)

@Serializable
data class GeoDto(
    val lat: String = "0",
    val lng: String = "0"
)
