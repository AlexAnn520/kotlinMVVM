package com.sample.kotlinmvvm.domain.model

/**
 * 用户领域模型
 *
 * 【面试要点】领域模型（Domain Model）不依赖任何框架注解（无 @Entity、@Serializable），
 * 保持纯 Kotlin 数据类，便于在各层之间传递且易于测试。
 * 与 DTO（网络层）和 Entity（数据库层）通过 Mapper 转换，实现层间解耦。
 */
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val company: String,
    val address: String
)
