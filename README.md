# 真心话大冒险·无限库

Android 本地真心话大冒险应用，支持自定义牌库、多人对战与历史存档。

## 当前版本功能（v1.0.0）

### 游戏桌
- 从任意牌库抽卡，支持翻牌动效
- 刺激等级滑块（Lv.1 ~ Lv.5），只抽不超过该等级的卡
- 标签筛选：情侣 / 双人 / 多人 / 全部
- 游戏模式切换：轮流抽（按玩家顺序）/ 指定抽
- 每次抽卡有震动反馈
- 保存本局记录到历史存档，或查看/清空当局流水账

### 牌库管理
- 创建多个牌库，每个牌库可设置名称和 emoji
- 卡牌字段：内容 / 类型（真心话、大冒险、道具卡）/ 刺激等级（1~5）/ 适用标签
- 添加、编辑、删除卡牌
- 长按牌库：重命名、复制、设为默认、导出短码、删除
- 导入：从本地 CSV / TXT 文件导入，或通过短码导入
- 导出：当前牌库导出为 CSV 文件，或生成短码分享

### 历史记录
- 查看所有已保存对局，显示牌库名、抽卡数、备注
- 展开查看完整抽卡流水（类型 + 内容）
- 支持关键字搜索过滤
- 单条删除或一键清空全部

### 设置
- 振动开关
- 深色模式 / 浅色模式切换

## 下载安装

前往 [Releases](https://github.com/acheng-byte/TruthOrDare/releases) 下载最新 APK，在 Android 设备上开启「允许安装未知来源」后直接安装。

- 最低系统要求：Android 8.0（API 26）
- 目标 SDK：Android 14（API 34）

## 技术栈

| 层 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI | View Binding + Material Design 3 |
| 架构 | MVVM（ViewModel + LiveData）|
| 数据库 | Room（SQLite）+ KSP |
| 导航 | Navigation Component |
| 协程 | Kotlin Coroutines |
| 构建 | Gradle + GitHub Actions |

## 构建

```bash
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/`

Release 构建通过推送 `v*` 格式 tag 自动触发 GitHub Actions，构建产物自动附到 GitHub Release。
