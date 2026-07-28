# Multi-version development

Totem Doll uses Stonecutter to build Fabric and NeoForge variants for each supported Minecraft version.

## Source layout

```text
common/
fabric/
neoforge/
versions/
├── 1.21.1/
└── 1.21.4/
```

Each directory under `versions/` represents one Minecraft version and supplies shared properties and optional version overrides. The `common`, `fabric`, and `neoforge` directories are Stonecutter branches; runnable Gradle projects are exposed below the loader branches, for example `:fabric:1.21.1`.

## Version differences

Use a Stonecutter preprocessor for a small API difference in an otherwise shared file. Use `//? >=1.21.4 {` for Minecraft API changes and `//? if fabric {` for platform-only changes. Put a complete replacement file in `versions/<version>/src/main/` only when the implementation is structurally different.

The effective source order is:

```text
common + fabric + versions/<version>/src/main
common + neoforge + versions/<version>/src/main
```

## Build targets

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :neoforge:1.21.1:build
.\gradlew.bat :fabric:1.21.4:build
.\gradlew.bat :neoforge:1.21.4:build
```

Artifacts use the form `Totem-Doll-<loader>-<minecraft-version>.jar`.
