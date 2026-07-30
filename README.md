# Totem Doll

Totem Doll is a cross-loader client-side Minecraft mod that turns the Totem of Undying into selectable, customizable dolls and other Minecraft-style models.

## Documentation

- [English documentation](docs/en/README.md)
- [中文文档](docs/zh/README.md)
- [Style pack template](style-template/README.md)

Press `F9` in game to open the Totem Doll style screen. Published style packs are imported as ZIP files. Developers can also place unpacked packs in the local styles directory while testing.

## Development

- Java 21
- Minecraft 1.21.1
- Fabric API (Fabric only)
- NeoForge
- Stonecutter

List the generated version/loader projects with:

```powershell
.\gradlew.bat projects
```

Build a specific target with:

```powershell
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :neoforge:1.21.1:build
```

The enabled version keeps its properties under `versions/1.21.1/`. Stonecutter exposes the shared `common`, `fabric`, and `neoforge` branches as indexed Gradle modules; runnable targets use `:fabric:1.21.1` and `:neoforge:1.21.1`. Forge and dedicated-server run configurations are not supported.

