# 光合作用 Photosynthesis — Android App

## 项目说明

心理健康行为激活 App 的 Android 版本。核心功能：用户拍摄自然场景照片 → 大模型 AI 分析四要素评分 → 植物获得光合值并成长。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **数据库**: Room (SQLite)
- **网络**: OkHttp (调用大模型API)
- **图片加载**: Coil
- **最低版本**: Android 8.0 (API 26)
- **目标设备**: 红米 K90

## 项目结构

```
app/src/main/java/com/photosynthesis/app/
├── PhotosynthesisApp.kt     # Application 入口
├── AppDatabase.kt           # Room 数据库定义
├── MainActivity.kt          # 唯一 Activity
├── data/
│   ├── CaptureRecord.kt     # 拍照记录实体
│   ├── CaptureRecordDao.kt  # 拍照记录 DAO
│   ├── PlantState.kt        # 植物状态实体
│   ├── PlantStateDao.kt     # 植物状态 DAO
│   ├── PlantGrowthEngine.kt # 植物生长算法引擎
│   └── PhotoAnalyzer.kt     # 大模型API照片分析
└── ui/
    ├── AppNavigation.kt     # 导航路由
    ├── theme/Theme.kt       # 主题配色
    └── screens/
        ├── SplashScreen.kt      # 启动页
        ├── ChoosePlantScreen.kt # 选择植物页
        ├── HomeScreen.kt        # 主页
        ├── CameraScreen.kt      # 拍摄页
        ├── ArchiveScreen.kt     # 光档案页
        └── ProfileScreen.kt     # 个人页(含API设置)
```

## 编译运行

### 前置条件
1. Android Studio Koala (2024.1.1) 或更高版本
2. JDK 17
3. Android SDK 34

### 步骤
1. 用 Android Studio 打开 `andriod-app` 文件夹
2. 等待 Gradle Sync 完成
3. 连接红米 K90（开启 USB 调试）或使用模拟器
4. 点击 Run ▶️

### 配置大模型 API
1. 打开 App → 进入主页 → 点击右上角头像进入「我的」
2. 点击右上角 ⚙️ 设置图标
3. 填入：
   - **API Key**: 你的大模型 API 密钥
   - **API URL**: 接口地址（默认 OpenAI 格式）
   - **Model**: 模型名称（如 gpt-4o-mini）

支持任何兼容 OpenAI Chat Completions API 格式的大模型服务。

## 核心机制

### 四要素评分 (每项 0-3 分)
- ☀️ **光 (Light)**: 画面亮度/阳光程度
- 💧 **水 (Water)**: 水元素存在感
- 🌤️ **气 (Air)**: 天空/空气开阔感
- 🌳 **境 (Biome)**: 植被/自然环境丰富度

### 植物生长阶段
种子(0) → 萌芽(10) → 幼苗(30) → 茁壮(80) → 开花(150) → 永生(300)

### 反打卡设计
- 不计连续天数
- 不量化心情
- 不显示分数排名
- 植物永不死亡
