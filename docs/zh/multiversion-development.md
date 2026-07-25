# 多版本开发

Totem Doll 使用 Stonecutter 管理 Fabric 和 NeoForge 的多个 Minecraft 版本。

## 源码结构

```text
common/
fabric/
neoforge/
versions/
├── 1.21.1-fabric/
├── 1.21.1-neoforge/
├── 1.21.4-fabric/
└── 1.21.4-neoforge/
```

根目录保存共享源码。`versions/` 下的每个目录都是一个 Stonecutter 构建目标。目录内的 `gradle.properties` 保存映射与平台依赖，`src/main/` 只保存该版本与平台需要覆盖的文件。

## 处理版本差异

同一文件只有少量 API 差异时，优先使用 Stonecutter 预处理。整份实现结构差异较大时，将替代文件放入：

```text
versions/<版本>-<平台>/src/main/
```

实际源码组合顺序为：

```text
common + fabric + versions/<版本>-fabric/src/main
common + neoforge + versions/<版本>-neoforge/src/main
```

## 构建目标

```powershell
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build
.\gradlew.bat :1.21.4-fabric:build
.\gradlew.bat :1.21.4-neoforge:build
```

产物命名格式为 `Totem-Doll-<平台>-<Minecraft版本>.jar`。
