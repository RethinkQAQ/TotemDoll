# 版本开发

Totem Doll 当前维护 Minecraft 1.21.1 与 1.21.3 的 Fabric、NeoForge common 节点。1.21.4 的渲染适配暂缓，当前优先完成与 Minecraft 版本无关的 `format:3` 迁移。项目采用 `multiloader-stonecutter` 的标准多加载器结构。

```text
common/
fabric/
neoforge/
versions/
├── 1.21.1/
│   └── gradle.properties
└── 1.21.3/
    └── gradle.properties
```

构建当前支持的目标：

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :fabric:1.21.3:build
.\gradlew.bat :neoforge:1.21.1:build
.\gradlew.bat :neoforge:1.21.3:build
```

未来新增版本时，在 `versions/<version>/` 中加入依赖属性，并在根目录 Stonecutter 属性中启用该版本；再按需要使用预处理或版本覆盖文件处理 API 差异。
