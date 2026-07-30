# 版本开发

Totem Doll 当前仅支持 Minecraft 1.21.1 的 Fabric 与 NeoForge。项目采用 `multiloader-stonecutter` 的标准多加载器结构，但目前只启用 `1.21.1` 节点。

```text
common/
fabric/
neoforge/
versions/
└── 1.21.1/
    └── gradle.properties
```

构建当前支持的目标：

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :neoforge:1.21.1:build
```

未来新增版本时，在 `versions/<version>/` 中加入依赖属性，并在根目录 Stonecutter 属性中启用该版本；再按需要使用预处理或版本覆盖文件处理 API 差异。
