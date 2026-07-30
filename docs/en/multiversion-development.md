# Version development

Totem Doll currently supports Minecraft 1.21.1 on Fabric and NeoForge. The project uses the standard Stonecutter multi-loader layout from `multiloader-stonecutter`; only the `1.21.1` node is enabled.

```text
common/
fabric/
neoforge/
versions/
└── 1.21.1/
    └── gradle.properties
```

Build the supported targets with:

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :neoforge:1.21.1:build
```

To add a future version, add its dependency properties under `versions/<version>/`, enable it through the root Stonecutter properties, then resolve API changes with Stonecutter preprocessing or a version override when necessary.
