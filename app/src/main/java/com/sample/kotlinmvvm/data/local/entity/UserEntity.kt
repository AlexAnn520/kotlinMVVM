package com.sample.kotlinmvvm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 数据库实体
 *
 * 【面试要点】Entity 与 DTO、Domain Model 三者分离：
 * - Entity: 数据库表结构，含 @Entity/@PrimaryKey 注解
 * - DTO: 网络响应结构，含 @Serializable 注解
 * - Domain Model: 纯数据类，无框架依赖
 * 这样任意一层的变化不会波及其他层。
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val company: String,
    val address: String
)
