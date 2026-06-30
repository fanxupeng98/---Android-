# NovelReader - Android 小说阅读器

一个功能完整的 Android 小说阅读应用，支持自定义书源、搜索、阅读和书架管理。

## 功能特性

### 核心功能
- 📚 **书架管理** - 本地书籍收藏，支持添加/删除/阅读
- 🔍 **多源搜索** - 支持多个书源同时搜索，过滤启用的书源
- 📖 **阅读器** - 流畅阅读体验，支持多种阅读主题和字体设置
- 🌐 **书源管理** - 添加、编辑、删除、导入导出书源
- 💾 **离线缓存** - 章节内容本地缓存，离线阅读

### 阅读器功能
- 字体大小调节（12-32sp）
- 三种阅读背景：白色、护眼（羊皮纸）、夜间
- 章节目录浏览
- 阅读进度记忆
- 左右滑动翻页

### 书源格式
书源采用 JSON + Base64 编码格式，便于分享和导入：
```json
{
  "name": "笔趣阁",
  "baseUrl": "https://example.com",
  "group": "默认",
  "searchUrl": "/s?kw={keyword}",
  "searchNameRule": ".book-name",
  "searchAuthorRule": ".author",
  "bookNameRule": ".title",
  "chapterListRule": ".chapter-list a"
}
```

## 技术架构

### 技术栈
- **语言**: Kotlin 1.9+
- **UI**: Jetpack Compose (Material Design 3)
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **本地数据库**: Room
- **网络请求**: OkHttp + Jsoup
- **异步**: Kotlin Coroutines + Flow

### 项目结构
```
app/src/main/java/com/novelreader/app/
├── NovelReaderApp.kt              # Application 入口
├── domain/                        # 领域层
│   ├── model/                      # 领域模型
│   │   ├── BookSource.kt          # 书源模型
│   │   ├── Book.kt                # 书籍模型
│   │   └── Chapter.kt             # 章节模型
│   └── repository/                 # 仓库接口
│       ├── Repositories.kt
├── data/                          # 数据层
│   ├── local/                      # 本地存储
│   │   ├── entity/                 # Room 实体
│   │   ├── dao/                    # Data Access Object
│   │   └── NovelReaderDatabase.kt  # 数据库
│   ├── remote/                     # 远程数据
│   │   ├── HtmlFetcher.kt          # HTML 抓取
│   │   └── BookParser.kt           # 解析器
│   └── repository/                 # 仓库实现
├── di/                            # 依赖注入
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
└── presentation/                   # 表现层
    ├── MainActivity.kt
    ├── navigation/
    │   ├── Screen.kt               # 路由定义
    │   └── NavGraph.kt             # 导航图
    ├── ui/
    │   ├── theme/                  # Compose 主题
    │   └── screens/                # 页面
    │       ├── bookshelf/          # 书架
    │       ├── search/             # 搜索
    │       ├── detail/             # 书籍详情
    │       ├── reader/             # 阅读器
    │       └── source/             # 书源管理
    └── viewmodel/                  # ViewModel
```

## 构建说明

### 环境要求
- JDK 17+
- Android SDK 34+
- Android Gradle Plugin 8.4+
- Gradle 8.4+

### 构建步骤

1. **安装 JDK 17**
   ```bash
   winget install Oracle.JDK.17
   ```

2. **安装 Android SDK**
   - 下载 Android Command Line Tools
   - 设置环境变量：
     ```bash
     set ANDROID_HOME=C:\Users\YourUser\AppData\Local\Android\Sdk
     set PATH=%PATH%;%ANDROID_HOME%\cmdline-tools\latest\bin
     ```
   - 安装 SDK 组件：
     ```bash
     sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"
     ```

3. **构建项目**
   ```bash
   cd NovelReader
   .\gradlew assembleDebug
   ```

4. **运行应用**
   - Debug APK 输出位置: `app/build/outputs/apk/debug/app-debug.apk`
   - 使用 `adb install app/build/outputs/apk/debug/app-debug.apk` 安装

## 导入书源

### 方式一：手动添加
1. 进入「书源」页面
2. 点击右下角「+」按钮
3. 填写书源信息（名称、URL、CSS 选择器等）
4. 点击保存

### 方式二：导入已有书源
1. 进入「书源」页面
2. 点击右上角「导入」按钮
3. 粘贴 Base64 编码的书源文本
4. 点击「导入」

### 导出书源
1. 进入「书源」页面
2. 点击书源右侧的「⋮」菜单
3. 选择「导出」
4. 复制生成的 Base64 文本分享给他人

## 书源编写指南

### 基本概念
书源定义了如何从某个网站抓取小说数据，使用 CSS 选择器定位页面元素。

### 必需字段
| 字段 | 说明 | 示例 |
|------|------|------|
| name | 书源名称 | 笔趣阁 |
| baseUrl | 网站根地址 | https://example.com |
| searchUrl | 搜索接口 | /s?kw={keyword} |
| searchNameRule | 书名选择器 | .book-title |
| bookNameRule | 详情页书名 | h1.title |

### 可选字段
| 字段 | 说明 |
|------|------|
| group | 书源分组 |
| searchAuthorRule | 搜索结果作者 |
| searchDetailUrlRule | 搜索结果详情链接 |
| bookAuthorRule | 详情页作者 |
| coverUrlRule | 封面图片 |
| introRule | 小说简介 |
| chapterListRule | 章节列表 |
| chapterNameRule | 章节名称 |
| contentRule | 正文内容 |

### CSS 选择器示例
```css
/* class 选择器 */
.book-title

/* ID 选择器 */
#book-name

/* 属性选择器 */
a[href*="novel"]

/* 组合选择器 */
.book-list .item .title

/* 伪选择器（支持部分）*/
.title:first-child
.content::text
```

## 截图预览

> 应用截图将在此处展示

## 许可证

本项目仅供学习交流使用，请勿用于商业目的。

## 致谢

- [Jetpack Compose](https://developer.android.com/compose) - 现代 Android UI 框架
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - 依赖注入
- [Room](https://developer.android.com/training/data-storage/room) - 本地数据库
- [OkHttp](https://square.github.io/okhttp/) - HTTP 客户端
- [Jsoup](https://jsoup.org/) - HTML 解析器
