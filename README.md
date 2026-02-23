# 微习惯启动器 (MicroHabit App)

一款为高压、持续加班人群设计的极简习惯记录工具。  
核心理念：**神经系统修复** 与 **防麻木**，而非「打卡坚持」。

---

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 主语言 |
| Jetpack Compose + Material 3 | UI 框架 |
| Room Database | 本地数据存储 |
| MVVM (ViewModel + StateFlow) | 架构模式 |
| DataStore Preferences | 熔断状态持久化 |
| Coroutines | 异步处理 |

---

## 项目结构

```
app/src/main/java/com/microhabit/app/
├── MainActivity.kt              # 入口 Activity
├── data/
│   ├── HabitRecord.kt           # Room Entity（数据模型）
│   ├── HabitDao.kt              # 数据库操作接口
│   └── HabitDatabase.kt         # Room Database 单例
├── viewmodel/
│   └── HabitViewModel.kt        # 业务逻辑 + 熔断机制
└── ui/
    ├── MainScreen.kt            # 首页（三锚点 + 熔断界面）
    ├── RecordBottomSheet.kt     # 打卡记录弹窗
    └── theme/
        └── Theme.kt             # Material 3 主题
```

---

## 核心功能

### 1. 三锚点打卡
- 起床锚点 / 下班锚点 / 睡前锚点
- 极简大按钮，最低操作阻力

### 2. 防麻木记录
- 1-5 档能量滑块
- 三组状态词强制单选（偏激活 / 偏抑制 / 平衡）
- 睡前锚点额外提供「一个念头」文本输入

### 3. 🔴 熔断机制（灵魂功能）
- **触发条件**：最近 5 天内，所有记录的状态词均为「木了」或「空了」，且能量分均低于 2 分
- **触发效果**：
  - 三锚点按钮全部禁用
  - 首页显示大字警告：「系统已熔断。这2天请停止所有微习惯，去睡觉。」
  - 熔断状态通过 DataStore 持久化，App 重启不丢失
- **自动解除**：48 小时后自动恢复正常

---

## 构建步骤（5分钟拿到 APK）

### 方法一：Android Studio（推荐）

1. 下载并安装 [Android Studio](https://developer.android.com/studio)（免费）
2. 打开 Android Studio → **File → Open** → 选择本项目根目录 `MicroHabitApp/`
3. 等待 Gradle 同步完成（首次需下载依赖，约 2-5 分钟）
4. 菜单 **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. 构建完成后点击弹窗中的 **locate** 找到 APK 文件
6. APK 路径：`app/build/outputs/apk/debug/app-debug.apk`
7. 通过数据线或文件传输将 APK 发送到手机，安装即可

### 方法二：命令行

```bash
cd MicroHabitApp
./gradlew assembleDebug
# APK 在 app/build/outputs/apk/debug/app-debug.apk
```

---

## 安装要求

- Android 8.0（API 26）及以上
- 手机需开启「允许安装未知来源应用」

---

## 设计理念说明

> 这款 App 不是让你「坚持打卡」的，而是一面神经系统状态的镜子。  
> 熔断机制的核心逻辑是：**当系统检测到你已经麻木到无法感受自己时，它会比你更早说「够了，去休息」。**
