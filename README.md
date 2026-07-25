# Totem Doll

Totem Doll is a cross-loader client-side Minecraft mod that turns the Totem of Undying into selectable, customizable dolls and other Minecraft-style models.

## Documentation

- [English documentation](docs/en/README.md)
- [中文文档](docs/zh/README.md)
- [Style pack template](style-template/README.md)

Press `F9` in game to open the Totem Doll style screen. Style packs can be distributed as ZIP files or imported as folders for development.

## Development

- Java 21
- Minecraft 1.21.1
- Fabric API (Fabric only)
- NeoForge

Build both supported loaders with:

```powershell
.\gradlew.bat :fabric:build :neoforge:build
```

