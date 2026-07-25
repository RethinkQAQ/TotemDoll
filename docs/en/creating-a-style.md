# Creating a Totem Doll Style

Copy `style-template/`, then edit the pack metadata, style metadata, model, and textures. Use a namespace you control, such as `author_name:my_doll`.

## Static item models

Use Blockbench's Minecraft Java Item/Block format and save the model as `models/main.json`. The model texture slots should match the slots in `style.json`, for example `#skin` and `textures.skin`.

## Bone models and animations

The `.bbmodel` file is an editing source; the game does not read it directly. Convert it into `geometry.json`, `animations.json`, and `style.json` using the repository converter:

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\my_style
```

See [Blockbench animation workflow](blockbench-animation.md) for bone names, transforms, interpolation, and triggers.

## Publishing

The ZIP root should contain either `pack.json` or `style.json`. The mod also accepts a ZIP with one outer directory.

Before publishing, test the inventory, first-person and third-person views, dropped items, item frames, transparency, second-layer skin geometry, and both Fabric and NeoForge.
