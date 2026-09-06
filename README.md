# 青稞 (ClassSchedule)

一款简约的课程表 App。以「周课程表」为核心，支持按周/单双周/自定义周排课、多选星期、自定义颜色，并可按学期配置作息时间。

## 功能

- **周课程表视图**：7 列 × N 节网格，课程块跨节合并，今日列高亮，显示每节起止时间
- **周次导航**：上一周 / 下一周 / 回到本周
- **课程管理**：多选星期、起止节次（上限随每天节数）、周次规则（全部 / 单数周 / 双数周 / 自定义周）
- **颜色标签**：4 浅色 + 3 深色 + 1 自定义颜色（可选任意颜色）
- **学期设置**：开学日期、总周数、每天节数；主题（跟随系统 / 浅色 / 深色）+ 动态取色
- **今日卡片**：显示真实日期当天的课程
- **数据备份**：JSON 导出 / 导入
- 本地数据持久化（Room），升级带迁移、不丢数据

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- Room（本地数据库）+ KSP
- Navigation Compose、DataStore
- MVVM + Repository + 手写 DI（无 Hilt）

## 构建

需要在装了 Android SDK 的环境中使用 Android Studio 或命令行构建：

```bash
./gradlew assembleDebug   # 或 gradlew.bat (Windows)
```

> `local.properties`（本机 SDK 路径）不随仓库提交，克隆后由 Android Studio 自动生成或手动填写 `sdk.dir`。

## 环境要求

- AGP 9.2.x，Kotlin 2.3.x，compileSdk 37，minSdk 24
