# 多版本开发

Totem Doll 使用 Stonecutter 管理 Fabric 和 NeoForge 的多个 Minecraft 版本。

## 源码结构

```text
common/
fabric/
neoforge/
versions/
├── 1.21.1/
└── 1.21.4/
```

`versions/` 下的目录只代表 Minecraft 版本，保存该版本的依赖属性和可选版本覆盖。Fabric 与 NeoForge 代码分别位于 `fabric/` 和 `neoforge/`。

## 版本差异

少量 Minecraft API 差异优先使用 Stonecutter 预处理：

```java
//? >=1.21.4 {
新版本代码
//?} else {
旧版本代码
//?}
```

只涉及平台的差异使用：

```java
//? if fabric {
Fabric 代码
//?} else {
NeoForge 代码
//?}
```

只有完整实现差异很大时，才将覆盖文件放入 `versions/<版本>/src/main/`。

实际源码组合为：

```text
common + fabric + versions/<版本>/src/main
common + neoforge + versions/<版本>/src/main
```

## 构建目标

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :neoforge:1.21.1:build
.\gradlew.bat :fabric:1.21.4:build
.\gradlew.bat :neoforge:1.21.4:build
```

产物命名格式为 `Totem-Doll-<平台>-<Minecraft版本>.jar`。
