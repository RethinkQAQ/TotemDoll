# 样式包格式

当前样式格式为 `format: 3`，包清单格式为 `format: 1`。样式格式与 Minecraft 版本无关，未知字段会被忽略。

## 目录

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

`pack.json` 的 `styles` 数组列出样式清单。单样式 ZIP 也可以直接以 `style.json` 为根。

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

`model.type` 固定为 `mesh`。所有模型和纹理路径都相对于当前 `style.json`，不允许绝对路径、命名空间路径或 `..`。

静态样式只提供 `geometry`；动画样式额外提供 `animations`。geometry 支持骨骼层级、pivot、cube、旋转、逐面 UV、mirror 和 display 变换。

## 显示上下文

display 可写在 `style.json` 或 `geometry.json`，style 优先。稳定上下文名为 `gui`、`ground`、`fixed`、`firstperson`、`thirdperson` 和 `head`。每项包含 `rotation`、`translation`、`scale` 三元数组。

## 动画

骨骼动画文件使用 tick、角度和倍率。顶层 `animations` 将动作绑定到 `loop`、`random_idle`、`on_screen_open`、`on_totem_activate` 或 `manual` 触发器。

纹理帧动画使用 `texture_animations`，帧名称必须引用 `textures` 中的逻辑槽。`frame_duration` 和随机区间都使用 tick。

## 工具链

Blockbench 是创作格式，游戏不直接读取 `.bbmodel`。使用：

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\example -StyleId author:example
```

转换器会生成 `format: 3` 的 style、geometry 和可选 animations。Minecraft 原生 `parent`、`overrides`、特殊模型和第三方 loader 不属于 mesh 格式。
