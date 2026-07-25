# Totem Doll Style Pack Format

The current style format is `format: 2` and the pack manifest format is `format: 1`. Unknown fields are ignored so the format can be extended later.

## Single-style and multi-style packs

A single-style pack contains `style.json` at its root:

```text
my-style/
├── style.json
├── models/
└── textures/
```

A multi-style pack uses `pack.json`:

```json
{
  "format": 1,
  "id": "example:character_pack",
  "name": "Character Pack",
  "author": "Author",
  "styles": [
    "styles/alex/style.json",
    "styles/robot/style.json"
  ]
}
```

Paths in `styles` are relative to `pack.json`. Absolute paths and paths containing `..` are not allowed.

## style.json

```json
{
  "format": 2,
  "id": "example:alex",
  "name": "Alex Doll",
  "model": {
    "type": "minecraft_item",
    "file": "models/main.json"
  },
  "textures": {
    "skin": "textures/base.png"
  },
  "skin": {
    "supported": true,
    "format": "minecraft_64x64",
    "target": "skin",
    "mapping": "minecraft_player"
  },
  "features": {
    "animations": false,
    "dynamic_textures": false
  }
}
```

- `id` must be a valid Minecraft ResourceLocation and unique among loaded styles.
- `name` is the display name when no language key is provided.
- `origin` is assigned by the mod and does not need to be included in a published pack.
- `template` is used for player-created derived styles.
- Relative PNG paths in `textures` are converted to Minecraft resource paths during import.

## Model types

`minecraft_item` uses a Blockbench Minecraft Java Item/Block JSON model and supports elements, per-face UVs, texture slots, and `display` transforms.

`minecraft_bone` uses `models/geometry.json` for bones and cubes and optionally `models/animations.json` for actions.

## Custom skins

Only styles with an explicit skin declaration show the create-style action:

```json
{
  "skin": {
    "supported": true,
    "format": "minecraft_64x64",
    "target": "base",
    "mapping": "minecraft_player"
  }
}
```

The target must match a texture slot. The current skin importer accepts 64×64 PNG files.

## Dynamic textures

Dynamic texture animations use named texture frames:

```json
{
  "textures": {
    "open": "textures/open.png",
    "closed": "textures/closed.png"
  },
  "features": {
    "animations": true,
    "dynamic_textures": true
  },
  "texture_animations": {
    "blink": {
      "type": "frame_sequence",
      "frames": ["open", "closed", "open"],
      "frame_duration": 3,
      "trigger": "random_idle",
      "interval": { "min": 80, "max": 180 }
    }
  }
}
```

`texture_animations` is reserved for texture frame sequences. Each animation name maps to texture-slot keys; it never contains bone action bindings. Supported triggers are `random_idle`, `loop`, `on_screen_open`, `on_totem_activate`, and `manual`.

For bone models, `model.animations` points to the Blockbench-exported bone animation file, while the top-level `animations` object contains only action bindings. Do not put texture frame sequences in that object. A texture animation can be used by both `minecraft_item` and `minecraft_bone` styles.
