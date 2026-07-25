# Multi-version development

Totem Doll uses Stonecutter to build the Fabric and NeoForge variants for each supported Minecraft version.

## Source layout

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

The root directories contain shared sources. Every directory under `versions/` is a real Stonecutter build target. Its `gradle.properties` supplies mappings and loader dependencies, while `src/main/` contains only files that override the shared implementation for that exact version and loader.

## Version differences

Use a Stonecutter preprocessor for a small API difference in an otherwise shared file. Put a complete replacement file in `versions/<version>-<loader>/src/main/` when the implementation is structurally different.

The effective source order is:

```text
common + fabric + versions/<version>-fabric/src/main
common + neoforge + versions/<version>-neoforge/src/main
```

## Build targets

```powershell
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build
.\gradlew.bat :1.21.4-fabric:build
.\gradlew.bat :1.21.4-neoforge:build
```

Artifacts use the form `Totem-Doll-<loader>-<minecraft-version>.jar`.
