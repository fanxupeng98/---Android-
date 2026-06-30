# Android 16 新特性适配 - 阅读 App

## 任务概述
为「阅读」小说应用适配 Android 16（API 35）新特性，重点改造状态栏沉浸、底部导航栏和阅读器全屏体验。

## 修改的文件

### 1. build.gradle.kts (app 层)
- `compileSdk`: 34 → **35**
- `targetSdk`: 34 → **35**
- 依赖版本全面升级：
  - `activity-compose`: 1.8.1 → **1.8.2**
  - `lifecycle-runtime-ktx`: 2.6.2 → **2.7.0**
  - `lifecycle-viewmodel-compose`: 2.6.2 → **2.7.0**
  - `navigation-compose`: 2.7.5 → **2.7.7**
  - `hilt-navigation-compose`: 1.1.0 → **1.2.0**
  - `core-splashscreen`: 新增 1.0.1
- Compose BOM: 2023.10.01 → **2024.02.00**

### 2. build.gradle.kts (根层)
- Gradle Wrapper: 8.4 → **8.6**（提升对 AGP 8.2 + SDK 35 兼容性）

### 3. res/values/themes.xml
- 启用边缘到边缘核心配置：
  ```xml
  android:windowTranslucentStatus = false
  android:windowTranslucentNavigation = false
  android:statusBarColor = @android:color/transparent
  android:navigationBarColor = @android:color/transparent
  android:enforceNavigationBarContrast = false
  android:enforceStatusBarContrast = false
  android:windowLayoutInDisplayCutoutMode = shortEdges
  ```

### 4. Theme.kt（主题核心）
- `WindowCompat.setDecorFitsSystemWindows(window, false)` 启用边缘到边缘
- `WindowCompat.getInsetsController` 控制状态栏/导航栏颜色跟随主题
- `statusBarColor` 和 `navigationBarColor` 设为透明

### 5. MainActivity.kt（主入口）
- `enableEdgeToEdge()` 启用边缘到边缘
- `WindowCompat.setDecorFitsSystemWindows(window, false)` 覆盖主题设置
- Android 15+: 关闭导航栏对比度强制 (`isNavigationBarContrastEnforced = false`)
- `contentWindowInsets` 根据页面类型动态调整：
  - 阅读页：`WindowInsets(0dp, 0dp, 0dp, 0dp)` 完全沉浸
  - 主页面：顶部系统栏 + 底部由玻璃导航栏覆盖
  - 其他页面：顶部沉浸 + 底部系统栏

### 6. GlassBottomNavigation.kt（新文件）
- **`LiquidGlassSurface`** 组件：iOS/miui 风格液态玻璃包装器
  - `Brush.verticalGradient` 实现从上到下的光泽渐变
  - `0.5dp` 半透明边框模拟玻璃边缘
  - `8dp` 柔和阴影模拟玻璃悬浮感
  - 可配置 `cornerRadius`（默认 20dp）
  - 可配置 `blurRadius`（默认 30dp）
  - 可配置 `overlayOpacity`（默认 0.6）
- **`GlassBottomNavigationBar`** 组件：iOS 风格底部导航
  - 高度 64dp + 底部悬浮偏移 28dp（留出小白条安全区）
  - 圆角 24dp（iOS 圆润风格）
  - 选中态光晕动画（spring 弹性动画）
  - `overlayOpacity` 0.72 增强磨砂质感
- **`GlassNavItem`** 数据类：支持 `selectedIcon`

### 7. MainActivity.kt - 底部导航替换
- `NavigationBar` → **`GlassBottomNavigationBar`**
- 底部 padding 设为 0dp（玻璃导航悬浮覆盖内容）

### 8. ReaderScreen.kt（阅读器）
- **`DisposableEffect`** 进入/退出全屏沉浸：
  - `WindowInsetsControllerCompat.hide(TYPE_SYSTEM_BARS())`
  - `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`（下滑临时显示系统栏）
  - Android 15+: 隐藏 `navigationBarBackground`
- 状态栏占位：`Spacer(height = getStatusBarHeight(view))`
- 小白条安全区：`Spacer(height = getNavBarHeight(view) + 16dp)`
- 三段式手势：左1/3=上一页，中间=显示控制，右1/3=下一页
- TopAppBar 背景设为 `Color.Transparent`

### 9. 各页面 TopAppBar 统一透明化
所有页面（书架/搜索/书源管理/添加书源/编辑书源/书籍详情）：
- `TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)`

## Android 16 适配要点

### 状态栏沉浸 ✅
- 所有主页面顶部完全透明，状态栏图标颜色跟随主题
- 阅读器进入时隐藏系统栏，3秒无操作后自动隐藏

### 小白条（导航条）适配 ✅
- 底部安全区动态计算 `navigation_bar_height`
- 阅读器底部内容留出小白条高度 + 额外 16dp
- 玻璃导航栏悬浮偏移 28dp，避开手势区域

### iOS 风格底部栏 ✅
- `LiquidGlassSurface` 实现磨砂玻璃效果
- 选中态径向光晕 + 弹性动画
- 高度 64dp，底部悬浮 28dp

### 液态玻璃效果 ✅
- `Brush.verticalGradient` 高光渐变
- 半透明边框 `0.5dp`
- 柔和阴影 `8dp`
- 可配置透明度、模糊度

## 技术细节

### 渐变配置
```kotlin
Brush.verticalGradient(
    colors = listOf(
        highlightColor.copy(alpha = overlayOpacity * 0.5f),  // 高光
        glassColor.copy(alpha = overlayOpacity),             // 主体
        glassColor.copy(alpha = overlayOpacity * 0.8f)       // 底部暗化
    )
)
```

### 边框配置
```kotlin
.border(
    width = 0.5.dp,
    color = borderColor,  // 亮色: 0x40FFFFFF / 暗色: 0x30FFFFFF
    shape = shape
)
```

### 沉浸模式
```kotlin
controller.hide(WindowInsetsCompat.Type.systemBars())
controller.systemBarsBehavior =
    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
```

---

# 阅读器翻页模式 & 自定义主题

## 新增功能

### 三种翻页模式

| 模式 | 实现方式 | 用户交互 |
|------|---------|---------|
| **仿真翻书** | `graphicsLayer` 3D透视 + `rotateY` + scale 动画 | 点击左/右侧区域翻页，支持拖拽 |
| **上下滚动** | `verticalScroll` + 章节导航按钮 | 中间区域滚动阅读，底部手动切换章节 |
| **左右滑动** | `HorizontalPager` 官方 Pager | 横向滑动翻页，类似阅读类 App |

### 仿真翻书实现

```kotlin
// graphicsLayer 实现翻书3D效果
.graphicsLayer {
    cameraDistance = 12f * density
    rotationY = direction * progress * 15f  // 3D透视旋转
    scaleX = 1f - (progress * 0.05f)
    scaleY = 1f - (progress * 0.05f)
    alpha = 1f - progress * 0.2f  // 翻页时渐隐
}
```

### 上下滚动实现

- `verticalScroll(rememberScrollState())` 实现无缝滚动
- 章节切换自动滚动到顶部 `scrollState.scrollTo(0)`
- 底部「上一章/下一章」按钮

### 左右滑动实现

```kotlin
HorizontalPager(state = pagerState) { page ->
    // 每页一个章节
}
```
- `rememberPagerState` 管理页面状态
- 切换章节时 `animateScrollToPage` 平滑跳转

### 自定义主题系统

#### 数据结构

```kotlin
data class ReaderTheme(
    val id: String,
    val name: String,
    val backgroundColor: Long,  // 背景色
    val textColor: Long,        // 文字色
    val accentColor: Long,      // 强调色
    val isBuiltIn: Boolean      // 是否内置（内置不可删除）
)
```

#### 预设主题（8个）

| 主题 | 背景色 | 文字色 | 强调色 |
|------|-------|-------|-------|
| 默认白 | `#FFFFFF` | `#333333` | `#6D5E0F` |
| 羊皮纸 | `#F5ECD7` | `#5C4B32` | `#8B7355` |
| 夜间模式 | `#121212` | `#CCCCCC` | `#DEC74C` |
| 海洋蓝 | `#1A2332` | `#B0C4DE` | `#4A90D9` |
| 森林绿 | `#1A2F1A` | `#90EE90` | `#228B22` |
| 少女粉 | `#FFF0F5` | `#6B4E5C` | `#FF69B4` |
| 星空紫 | `#2D1B3D` | `#DDA0DD` | `#9370DB` |
| 奢华金 | `#2C2416` | `#D4AF37` | `#FFD700` |

#### 背景颜色预设（5个）

`ReaderBackground` 枚举包含：White、Sepia、Night、DarkBlue、DarkGreen

#### 主题编辑器界面

- **网格展示**：4列主题卡片，每张预览背景/文字/强调色
- **选中态**：2dp 强调色边框
- **自定义主题**：内置不可删除，自定义可删除
- **随机生成**：一键生成随机自定义主题

### 阅读器设置面板

```
┌─────────────────────────────────┐
│  翻页模式                          │
│  [仿真翻书] [上下滚动] [左右滑动]     │
├─────────────────────────────────┤
│  字体大小  [Aa-] 18 [Aa+]         │
│  行间距  ──●─────────            │
├─────────────────────────────────┤
│  背景色  ○ ○ ○ ○ ○               │
├─────────────────────────────────┤
│  [🔘 自定义主题]                  │
└─────────────────────────────────┘
```
