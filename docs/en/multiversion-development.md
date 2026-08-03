# Version development

Totem Doll currently maintains Minecraft 1.21.1 and 1.21.3 common nodes on Fabric and NeoForge. The 1.21.4 rendering adapter is deferred while the version-independent `format:3` migration is completed. The project uses the standard Stonecutter multi-loader layout from `multiloader-stonecutter`.

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

Build the supported targets with:

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :fabric:1.21.3:build
.\gradlew.bat :neoforge:1.21.1:build
.\gradlew.bat :neoforge:1.21.3:build
```

To add a future version, add its dependency properties under `versions/<version>/`, enable it through the root Stonecutter properties, then resolve API changes with Stonecutter preprocessing or a version override when necessary.
