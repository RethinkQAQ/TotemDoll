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

Bone animation files use ticks, degrees, and scale multipliers. One tick is 0.05 seconds. Top-level `animations` bind actions to `loop`, `random_idle`, `on_screen_open`, `on_totem_activate`, or `manual` triggers.

Each action binding may also define:

- `priority`: the action priority used when several bindings share a trigger.
- `interrupt`: when `true`, immediately interrupts the current action; when `false` or omitted, waits for the current action to finish. An unconfigured `on_totem_activate` action defaults to interrupting immediately.
- `interval`: the random wait range for `random_idle`, in ticks. The default is 80–180 ticks.
- `texture_animation`: binds a texture frame animation. The texture starts when the bone action actually starts and stops when the action ends.

For example:

```json
{
  "animations": {
    "screen_wave": {
      "animation": "screen_wave",
      "trigger": "on_screen_open",
      "priority": 60,
      "interrupt": true
    },
    "sneak": {
      "animation": "sneak",
      "trigger": "random_idle",
      "texture_animation": "sneak_texture"
    }
  }
}
```

Texture sequences use `texture_animations`; frame names must reference logical entries in `textures`. Frame durations and random intervals use ticks.

Texture animations support these triggers:

- `loop`: play continuously.
- `random_idle`: play independently after a random wait.
- `on_screen_open`, `on_totem_activate`, `manual`: start from the corresponding event.
- `linked`: start only through a bone action's `texture_animation` field and never use an independent random timer.

The linked texture follows the bone action's lifetime. If the bone action loops, the linked texture also loops. When independent and linked texture animations coexist, the active linked texture takes precedence.

## Tooling

Blockbench remains the authoring format; the game does not read `.bbmodel` directly:

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\example -StyleId author:example
```

The converter emits a `format: 3` style, geometry, and optional animations. Minecraft-native parents, overrides, special models, and third-party loaders are outside the mesh format.
