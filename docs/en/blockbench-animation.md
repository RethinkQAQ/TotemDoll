# Blockbench Animation Workflow

Totem Doll does not read `.bbmodel` files at runtime. Use Blockbench to edit static or animated meshes, then export the version-independent `format: 3` runtime files `geometry.json`, `animations.json`, and `style.json`.

## Recommended hierarchy

```text
root
└── body
    ├── head
    ├── left_arm
    ├── right_arm
    ├── left_leg
    └── right_leg
```

Bone names referenced by `animations.json` must match the geometry exactly. Pivots, cube coordinates, and UVs use Blockbench model units. Rotation keyframes use degrees; position uses model units; scale uses multipliers.

The runtime supports `linear`, `step`, and `smooth` interpolation. Bone action triggers are `loop`, `random_idle`, `on_screen_open`, `on_totem_activate`, and `manual`. Set `interrupt: true` on an action binding to interrupt the current action immediately; omit it or set it to `false` to wait for the current action to finish. Link a texture animation through `texture_animation` and use `trigger: "linked"` on the referenced entry in `texture_animations`.

The runnable example is in `style-template/styles/animated_example/`. Animated styles can also support custom skins by setting `skin.supported` to `true` and targeting the base texture slot.
