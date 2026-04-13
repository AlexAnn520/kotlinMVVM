package com.sample.kotlinmvvm.data.mapper

import com.sample.kotlinmvvm.data.local.entity.UserEntity
import com.sample.kotlinmvvm.data.remote.dto.UserDto
import com.sample.kotlinmvvm.domain.model.User

/**
 * 数据层映射函数 — DTO / Entity / Domain 之间的转换
 *
 * 【面试要点】使用扩展函数做 Mapper 的优势：
 * 1. 代码简洁，无需额外 Mapper 类
 * 2. 转换逻辑集中管理，修改一处即可
 * 3. 各层模型独立演进，互不影响
 */

/** 网络 DTO → 数据库 Entity */
fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    username = username,
    email = email,
    phone = phone,
    website = website,
    company = company.name,
    address = "${address.city}, ${address.street}"
)

/** 数据库 Entity → 领域模型 */
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    username = username,
    email = email,
    phone = phone,
    website = website,
    company = company,
    address = address
)

/** 网络 DTO → 领域模型（跳过数据库场景使用） */
fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    username = username,
    email = email,
    phone = phone,
    website = website,
    company = company.name,
    address = "${address.city}, ${address.street}"
)
