# Totem Doll

Totem Doll is a cross-loader client-side Minecraft mod that turns the Totem of Undying into selectable, customizable dolls and other Minecraft-style models.

## Documentation

- [English documentation](docs/en/README.md)
- [中文文档](docs/zh/README.md)
- [Style pack template](style-template/README.md)

Press `F9` in game to open the Totem Doll style screen. Published style packs are imported as ZIP files. Developers can also place unpacked packs in the local styles directory while testing.

## Development

- Java 21
- Minecraft 1.21.1 and 1.21.4
- Fabric API (Fabric only)
- NeoForge
- Stonecutter

List the generated version/loader projects with:

```powershell
.\gradlew.bat projects
```

Build a specific target with:

```powershell
.\gradlew.bat :1.21.1-fabric:build
.\gradlew.bat :1.21.1-neoforge:build
.\gradlew.bat :1.21.4-fabric:build
.\gradlew.bat :1.21.4-neoforge:build
```

Each Stonecutter target lives under `versions/<minecraft-version>-<loader>/` and contains its target properties and optional source overrides. The root `common/`, `fabric/`, and `neoforge/` directories remain shared. Forge and dedicated-server run configurations are not supported.

