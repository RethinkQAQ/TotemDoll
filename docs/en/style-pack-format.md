# Style pack format

The current style format is `format: 3`; the pack manifest remains `format: 1`. The style format is independent of Minecraft versions and unknown fields are ignored.

## Layout

```text
pack.json
styles/example/
├── style.json
├── models/
│   ├── geometry.json
│   └── animations.json
└── textures/
    └── base.png
```

`pack.json` lists style manifests in its `styles` array. A single-style ZIP may also place `style.json` at its root.

## style.json

```json
{
  "format": 3,
  "id": "author:example",
  "name": "Example Doll",
  "model": {
    "type": "mesh",
    "geometry": "models/geometry.json",
    "animations": "models/animations.json"
  },
  "textures": { "base": "textures/base.png" },
  "skin": {
    "supported": true,
    "format": "minecraft_64x64",
    "target": "base",
    "mapping": "minecraft_player"
  },
  "features": { "animations": true, "dynamic_textures": false },
  "animations": {}
}
```

`model.type` is always `mesh`. Every model and texture path is relative to the current `style.json`; absolute, namespaced, and `..` paths are rejected.

Static styles provide geometry only. Animated styles also provide an animation file. Geometry supports bone hierarchies, pivots, cubes, rotations, per-face UVs, mirroring, and display transforms.

## Display contexts

Display transforms may be declared in `style.json` or `geometry.json`, with style taking precedence. Stable context names are `gui`, `ground`, `fixed`, `firstperson`, `thirdperson`, and `head`. Each transform contains `rotation`, `translation`, and `scale` vectors.

## Animation

Bone animation files use ticks, degrees, and scale multipliers. Top-level `animations` bind actions to `loop`, `random_idle`, `on_screen_open`, `on_totem_activate`, or `manual` triggers.

Texture sequences use `texture_animations`; frame names must reference logical entries in `textures`. Frame durations and random intervals use ticks.

## Tooling

Blockbench remains the authoring format; the game does not read `.bbmodel` directly:

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\example -StyleId author:example
```

The converter emits a `format: 3` style, geometry, and optional animations. Minecraft-native parents, overrides, special models, and third-party loaders are outside the mesh format.
