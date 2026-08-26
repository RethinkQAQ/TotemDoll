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

Display transforms may be declared in `style.json` or `geometry.json`, with style taking precedence. Stable context names are `gui`, `ground`, `fixed`, `firstperson`, `firstperson_righthand`, `firstperson_lefthand`, `thirdperson`, `thirdperson_righthand`, `thirdperson_lefthand`, `head`, and `on_shelf`. Each transform contains `rotation`, `translation`, and `scale` vectors.

### Mod compatibility display overrides

A style may override display transforms when selected mods are loaded. Mod checks happen only while resources are loaded or reloaded, not during rendering:

```json
"compatibility": {
  "display_overrides": [
    {
      "mods": ["firstperson", "punchy"],
      "match": "any",
      "contexts": {
        "thirdperson_righthand": {
          "rotation": [70, 0, 0],
          "translation": [0, 1.5, 1.5],
          "scale": [0.5, 0.5, 0.5]
        },
        "thirdperson_lefthand": {}
      }
    }
  ]
}
```

`match` defaults to `any`. Setting `perspective` to `firstperson` limits an override to the first-person camera; omitting it preserves the general behavior. Matching entries are merged in array order; later fields override earlier fields and omitted fields inherit the base display transform. Compatibility overrides affect display transforms only, not models, textures, or animations.

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
