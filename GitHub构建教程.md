# 🚀 用 GitHub Actions 自动构建 APK 完整教程

无需安装 Android Studio，全程在浏览器操作，约 10 分钟拿到 APK。

---

## 第一步：初始化项目（在本机用命令行，约 2 分钟）

> 这一步只需做一次。如果你没有装 Git，先到 https://git-scm.com 下载安装。

打开终端（Windows 用 PowerShell 或 CMD），执行：

```bash
# 1. 进入解压后的项目目录
cd 你解压的路径/MicroHabitApp

# 2. 初始化 git 仓库
git init
git add .
git commit -m "初始化微习惯启动器项目"
```

---

## 第二步：在 GitHub 创建仓库并推送代码

**方式 A：命令行（推荐）**

```bash
# 1. 先在 GitHub 网页创建一个新仓库（不要勾选任何初始化选项）
#    地址：https://github.com/new
#    仓库名随意，例如：micro-habit-app

# 2. 回到终端，添加远程仓库并推送
git remote add origin https://github.com/你的用户名/micro-habit-app.git
git branch -M main
git push -u origin main
```

**方式 B：用 GitHub Desktop（图形界面，更简单）**

1. 下载 [GitHub Desktop](https://desktop.github.com/)
2. File → Add Local Repository → 选择项目文件夹
3. 点 "Publish repository" → 填写仓库名 → Publish

---

## 第三步：⚠️ 关键步骤 —— 添加 gradle-wrapper.jar

GitHub Actions 构建时需要这个文件，但它是二进制文件，项目中已通过 .gitignore 排除。

**在 GitHub 网页上手动添加：**

1. 打开你的仓库页面
2. 进入目录：`gradle/wrapper/`
3. 点击右上角 **Add file → Upload files**
4. 上传从以下地址下载的文件：
   👉 https://github.com/gradle/gradle/blob/v8.4.0/gradle/wrapper/gradle-wrapper.jar
   
   （点页面右侧 Download raw file 按钮下载）
5. 提交：填写 commit 信息 → Commit changes

---

## 第四步：触发自动构建

代码推送到 main 分支后，GitHub Actions **会自动开始构建**。

你也可以手动触发：
1. 打开仓库页面，点击顶部 **Actions** 标签
2. 左侧选择 **Build APK**
3. 点击右侧 **Run workflow → Run workflow**

---

## 第五步：下载 APK

1. 点击 **Actions** 标签
2. 点击最新的一条构建记录（绿色 ✅ 表示成功）
3. 页面底部找到 **Artifacts** 区域
4. 点击 **微习惯启动器-debug** 下载 zip
5. 解压 zip，里面就是 `app-debug.apk`

---

## 第六步：安装到手机

**Android 手机：**
1. 设置 → 安全 → 开启「允许安装未知来源应用」
2. 将 APK 文件传到手机（微信发给自己 / 数据线 / 蓝牙）
3. 点击 APK 文件 → 安装

---

## 常见问题

### ❌ Actions 构建失败，显示红色 ✗

点击失败的任务查看日志，常见原因：
- **gradle-wrapper.jar 未上传** → 按第三步操作
- **Gradle 版本不兼容** → 检查 `gradle/wrapper/gradle-wrapper.properties` 中的版本号

### ❌ 推送代码时要求输入密码

GitHub 已不支持密码认证，需要用 Personal Access Token：
1. GitHub → Settings → Developer settings → Personal access tokens → Generate new token
2. 勾选 `repo` 权限
3. 用生成的 token 替代密码

### ⏱ 构建要多久？

首次约 **5-8 分钟**（需要下载 Gradle 和所有依赖）。  
之后因为有缓存，约 **2-3 分钟**。

---

## 完整文件结构（供参考）

```
MicroHabitApp/
├── .github/
│   └── workflows/
│       └── build.yml          ← GitHub Actions 配置（已包含）
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/microhabit/app/
│   │       ├── MainActivity.kt
│   │       ├── data/          ← Room Database
│   │       ├── viewmodel/     ← 熔断逻辑
│   │       └── ui/            ← Compose 界面
│   └── build.gradle.kts
├── gradle/wrapper/
│   ├── gradle-wrapper.jar     ← ⚠️ 需手动上传
│   └── gradle-wrapper.properties
├── gradlew                    ← 构建脚本
├── build.gradle.kts
└── settings.gradle.kts
```
