# KotlinMvvm — Android MVVM 最佳实践

基于 Kotlin + Jetpack Compose + Material Design 3 的 MVVM 架构最佳实践项目，涵盖面试高频考点。

## 技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Kotlin | 2.0.21 | 开发语言 |
| Jetpack Compose | BOM 2024.09.00 | 声明式 UI |
| Material Design 3 | Compose BOM | UI 组件和主题 |
| Hilt | 2.56.1 | 依赖注入 |
| Retrofit | 2.11.0 | 网络请求 |
| OkHttp | 4.12.0 | HTTP 引擎 |
| kotlinx-serialization | 1.7.3 | JSON 序列化（替代 Gson） |
| Room | 2.6.1 | 本地数据库 |
| Navigation Compose | 2.8.5 | 类型安全导航 |
| Kotlin Coroutines | 1.9.0 | 异步编程 |
| StateFlow | Coroutines 内置 | 状态管理（替代 LiveData） |
| KSP | 2.0.21-1.0.28 | 注解处理 |
| MockK | 1.13.13 | 单元测试 Mock |
| Turbine | 1.2.0 | Flow 测试 |

## 架构概览

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│  Compose Screen ← collectAsStateWithLifecycle()  │
│        ↕ 事件回调                                 │
│  ViewModel (StateFlow + Channel)                 │
├─────────────────────────────────────────────────┤
│                 Domain Layer                     │
│  Repository 接口 ← 依赖倒置                      │
│  Domain Model ← 纯 Kotlin 数据类                 │
├─────────────────────────────────────────────────┤
│                  Data Layer                      │
│  Repository 实现 (离线优先)                       │
│  ┌──────────┐    ┌──────────┐                   │
│  │ Retrofit  │───→│   Room   │───→ Flow 通知 UI  │
│  │ (Network) │    │  (Local) │                   │
│  └──────────┘    └──────────┘                   │
├─────────────────────────────────────────────────┤
│                  DI Layer (Hilt)                 │
│  AppModule · NetworkModule · DatabaseModule      │
└─────────────────────────────────────────────────┘
```

**数据流向**：Network API → Room DB → Flow → ViewModel → Compose UI

## 包结构

```
com.sample.kotlinmvvm
├── App.kt                              # @HiltAndroidApp 应用入口
├── MainActivity.kt                     # @AndroidEntryPoint 单 Activity
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              # Room 数据库定义
│   │   ├── dao/UserDao.kt              # DAO（Flow 返回值自动观察）
│   │   └── entity/UserEntity.kt        # 数据库实体
│   ├── remote/
│   │   ├── api/UserApi.kt              # Retrofit suspend 接口
│   │   └── dto/UserDto.kt              # @Serializable 网络 DTO
│   ├── mapper/UserMappers.kt           # DTO ↔ Entity ↔ Domain 映射
│   └── repository/UserRepositoryImpl.kt # 离线优先 Repository
├── di/
│   ├── AppModule.kt                    # Dispatcher + Repository 绑定
│   ├── NetworkModule.kt                # OkHttp + Retrofit + Json
│   └── DatabaseModule.kt               # Room 数据库
├── domain/
│   ├── model/User.kt                   # 纯领域模型
│   └── repository/UserRepository.kt    # Repository 接口
├── ui/
│   ├── component/                      # 可复用 Compose 组件
│   │   ├── LoadingContent.kt
│   │   ├── ErrorContent.kt
│   │   └── UserCard.kt
│   ├── navigation/
│   │   ├── Route.kt                    # @Serializable 类型安全路由
│   │   └── AppNavigation.kt            # NavHost 导航图
│   ├── screen/
│   │   ├── userlist/
│   │   │   ├── UserListUiState.kt      # sealed interface 状态
│   │   │   ├── UserListViewModel.kt    # StateFlow + Channel
│   │   │   └── UserListScreen.kt       # 列表页（下拉刷新）
│   │   └── userdetail/
│   │       ├── UserDetailUiState.kt
│   │       ├── UserDetailViewModel.kt  # SavedStateHandle 取参
│   │       └── UserDetailScreen.kt     # 详情页
│   └── theme/                          # Material 3 主题
└── util/
    └── UiEvent.kt                      # 一次性事件（Snackbar）
```

## 关键架构决策

### 1. StateFlow 替代 LiveData

| 对比项 | StateFlow | LiveData |
|---|---|---|
| 依赖 | Kotlin 标准库 | Android 框架 |
| 初始值 | 必须有（类型安全） | 可为 null |
| 操作符 | map/combine/filter 等 | 需借助 Transformations |
| 多模块 / KMM | 原生支持 | 仅 Android |
| Compose 集成 | collectAsStateWithLifecycle() | observeAsState() |

**结论**：现代 Kotlin + Compose 项目推荐 StateFlow。

### 2. sealed interface 替代 sealed class

```kotlin
sealed interface UserListUiState {
    data object Loading : UserListUiState
    data class Success(val users: List<User>) : UserListUiState
    data class Error(val message: String) : UserListUiState
    data object Empty : UserListUiState
}
```

优势：
- 允许实现多个密封层级
- `data object` 自动生成 `toString()`
- 编译器强制 `when` 覆盖所有分支

### 3. 离线优先 Repository（单一数据源）

```
核心原则：Room 是唯一的"真相源"（Single Source of Truth）
- UI 只观察数据库 Flow
- 网络数据写入数据库后，Flow 自动通知 UI 更新
- 好处：数据一致性 + 天然离线支持
```

### 4. 一次性事件处理

```kotlin
// ViewModel 中
private val _uiEvent = Channel<UiEvent>()
val uiEvent = _uiEvent.receiveAsFlow()

// Compose 中
LaunchedEffect(Unit) {
    viewModel.uiEvent.collect { event ->
        when (event) {
            is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }
}
```

为什么用 Channel 而非 StateFlow？StateFlow 有 replay，新订阅者会收到最后一个值，Snackbar 会重复显示。

### 5. 类型安全导航

```kotlin
// 路由定义
@Serializable
data class UserDetail(val userId: Int) : Route

// 导航
navController.navigate(Route.UserDetail(userId))

// ViewModel 取参
val userId: Int = checkNotNull(savedStateHandle["userId"])
```

编译时校验参数类型，避免字符串拼接导致的运行时崩溃。

### 6. Kotlin Serialization 替代 Gson

| 对比项 | kotlinx-serialization | Gson |
|---|---|---|
| 解析方式 | 编译时代码生成 | 运行时反射 |
| 性能 | 更快 | 较慢 |
| 空安全 | 编译时检查 | 运行时崩溃 |
| Kotlin 兼容 | 原生支持 data class | 需 adapter |

## 面试高频考点速查

### Q1: 为什么选 MVVM 而非 MVP/MVC？

**答**：MVVM 的核心优势在于：
1. **关注点分离**：View 只管 UI 展示，ViewModel 管理业务逻辑，Model 处理数据
2. **可测试性**：ViewModel 不持有 View 引用，可直接 JVM 单元测试
3. **生命周期感知**：ViewModel 存活于配置变更（如屏幕旋转），数据不丢失
4. **数据驱动 UI**：Compose + StateFlow 实现声明式 UI，状态变化自动触发重组

→ 代码参考：`UserListViewModel.kt`

### Q2: StateFlow vs LiveData 什么时候用哪个？

**答**：
- **StateFlow**：Kotlin-first，无 Android 依赖，支持 Flow 操作符，适合 Compose 项目
- **LiveData**：与 XML DataBinding 配合更好，Java 项目更友好

现代 Kotlin + Compose 项目推荐 StateFlow + `collectAsStateWithLifecycle()`。

→ 代码参考：`UserListViewModel.kt` 顶部注释

### Q3: ViewModel 如何存活配置变更？

**答**：
1. ViewModel 存储在 `ViewModelStore` 中
2. `ViewModelStore` 通过 `NonConfigurationInstances` 机制在 Activity 重建时保留
3. Activity 调用 `onDestroy()` 时，如果是配置变更（`isChangingConfigurations=true`），ViewModelStore **不会清除**
4. 新 Activity 创建时从保留的 ViewModelStore 中获取同一个 ViewModel 实例
5. `viewModelScope` 绑定 ViewModel 生命周期，`onCleared()` 时自动取消所有协程

→ 代码参考：`UserListViewModel.kt` 注释

### Q4: 屏幕间如何传递数据？

**答**：
1. **Navigation 参数**（推荐）：通过类型安全路由传递简单参数，ViewModel 从 SavedStateHandle 提取
2. **共享 ViewModel**：同一 NavGraph 内共享 ViewModel，适合多页面共用复杂状态
3. **Repository 层**：通过共享 Repository 的 Flow，各页面独立观察同一数据源

→ 代码参考：`Route.kt` + `UserDetailViewModel.kt`

### Q5: Repository 模式和单一数据源是什么？

**答**：
- Repository 是 Data 层的门面，对外提供统一的数据访问接口
- 单一数据源（SSOT）：Room 数据库是唯一的"真相源"
  - 网络数据 → 写入 DB → UI 观察 DB Flow
  - 避免内存中多份数据不同步
  - 天然支持离线

→ 代码参考：`UserRepositoryImpl.kt`

### Q6: 协程异常怎么处理？

**答**：
1. `viewModelScope.launch { try/catch }` — 最推荐，可控性最强
2. `Flow.catch {}` — 捕获 Flow 上游异常
3. `CoroutineExceptionHandler` — 仅做日志，无法恢复，不推荐做主要处理方式

→ 代码参考：`UserListViewModel.kt` 的 `loadUsers()` 和 `refresh()`

### Q7: 一次性事件（如 Snackbar）怎么处理？

**答**：
- **Channel + receiveAsFlow()**：保证事件只消费一次，无订阅者时也会缓存
- **SharedFlow(replay=0)**：适合可丢弃事件
- ~~Event 包装类~~：LiveData 时代产物，Compose 中不推荐

→ 代码参考：`UiEvent.kt` + `UserListViewModel.kt` + `UserListScreen.kt`

### Q8: 如何测试 ViewModel？

**答**：
1. **替换 Dispatchers.Main**：使用 `MainDispatcherRule` + `UnconfinedTestDispatcher`
2. **Fake Repository**：实现接口，设置假数据（比 Mock 更可读）
3. **Turbine**：测试 StateFlow 发射顺序（`test { awaitItem() }`）
4. **runTest {}**：提供测试协程作用域

→ 代码参考：`UserListViewModelTest.kt` + `FakeUserRepository.kt` + `MainDispatcherRule.kt`

## 构建与运行

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 运行全部单元测试
./gradlew test

# 运行指定测试类
./gradlew test --tests "com.sample.kotlinmvvm.ui.screen.userlist.UserListViewModelTest"

# 清理并重新编译
./gradlew clean assembleDebug
```

## API 说明

使用 [JSONPlaceholder](https://jsonplaceholder.typicode.com/) 公共 API：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/users` | GET | 获取用户列表（10 条） |
| `/users/{id}` | GET | 获取单个用户详情 |

## 项目配置

| 配置项 | 值 |
|---|---|
| Compile SDK | 36 |
| Min SDK | 24 |
| Target SDK | 36 |
| Java Target | 11 |
| Kotlin | 2.0.21 |
| AGP | 8.12.1 |
