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

display 可写在 `style.json` 或 `geometry.json`，style 优先。稳定上下文名为 `gui`、`ground`、`fixed`、`firstperson`、`firstperson_righthand`、`firstperson_lefthand`、`thirdperson`、`thirdperson_righthand`、`thirdperson_lefthand`、`head` 和 `on_shelf`。每项包含 `rotation`、`translation`、`scale` 三元数组。

### 模组兼容显示覆盖

样式可以根据已加载的模组覆盖显示变换。模组检测只在资源加载和重载时进行，渲染过程中不会重复查询：

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

`match` 缺省时为 `any`。设置 `perspective` 为 `firstperson` 后，覆盖只在第一人称相机中生效；未设置时保持通用行为。多个匹配项按数组顺序合并，后面的已填写字段覆盖前面的字段，未填写字段继承基础 display。兼容覆盖只影响显示变换，不改变模型、纹理或动画。

## 动画

骨骼动画文件使用 tick、角度和倍率。1 tick 等于 0.05 秒。顶层 `animations` 将动作绑定到 `loop`、`random_idle`、`on_screen_open`、`on_totem_activate` 或 `manual` 触发器。

每个动作绑定还可以使用：

- `priority`：动作优先级，用于选择同一触发器下的动作。
- `interrupt`：设为 `true` 时立即打断当前动作；设为 `false` 或省略时等待当前动作播放完毕。未配置的 `on_totem_activate` 默认立即打断。
- `interval`：`random_idle` 的随机等待区间，单位为 tick，默认值为 80～180。
- `texture_animation`：绑定一个纹理帧动画。纹理会在骨骼动作真正开始时同步启动，并在动作结束时停止。

例如：

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

纹理帧动画使用 `texture_animations`，帧名称必须引用 `textures` 中的逻辑槽。`frame_duration` 和随机区间都使用 tick。

纹理动画支持以下触发方式：

- `loop`：持续循环播放。
- `random_idle`：独立随机播放。
- `on_screen_open`、`on_totem_activate`、`manual`：由对应事件启动。
- `linked`：只由骨骼动作的 `texture_animation` 字段启动，不使用自己的随机计时器。

联动纹理的播放生命周期跟随骨骼动作；如果骨骼动作循环，联动纹理也会循环。已有独立纹理动画与联动纹理同时存在时，当前联动纹理优先显示。

## 工具链

Blockbench 是创作格式，游戏不直接读取 `.bbmodel`。使用：

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\example -StyleId author:example
```

转换器会生成 `format: 3` 的 style、geometry 和可选 animations。Minecraft 原生 `parent`、`overrides`、特殊模型和第三方 loader 不属于 mesh 格式。
