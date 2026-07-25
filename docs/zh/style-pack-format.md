# Totem Doll 样式包格式

当前样式格式为 `format: 2`，包清单格式为 `format: 1`。未知字段会被忽略，方便未来扩展。

## 单样式与多样式

单样式包根目录直接包含 `style.json`：

```text
my-style/
├── style.json
├── models/
└── textures/
```

多样式包通过 `pack.json` 声明样式：

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

`styles` 中的路径相对于 `pack.json`。路径必须是相对路径，不能包含 `..`。

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

- `id` 必须是合法的 Minecraft ResourceLocation，并在所有已加载样式中唯一。
- `name` 是没有语言文件时显示的名称；内置资源也可以使用 `name_key`。
- `origin` 由模组决定，发布包无需填写。
- `template` 仅用于玩家创建的派生样式。
- `textures` 的值推荐使用相对于当前 `style.json` 的 PNG 路径。导入器会转换为当前 Minecraft 版本使用的资源路径。

## 模型类型

### minecraft_item

```json
{
  "model": {
    "type": "minecraft_item",
    "file": "models/main.json"
  }
}
```

模型是 Blockbench 的 Minecraft Java Item/Block JSON。支持元素、每面 UV、多纹理槽和 `display`。

### minecraft_bone

```json
{
  "model": {
    "type": "minecraft_bone",
    "geometry": "models/geometry.json",
    "animations": "models/animations.json"
  }
}
```

`geometry.json` 保存骨骼、立方体和显示变换；`animations.json` 保存动作。动画文件可省略，此时模型使用静态姿态。

## 自定义皮肤

只有明确声明以下字段的样式才会显示“创建”：

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

`target` 必须与 `textures` 中的纹理槽一致。当前只接受 64×64 PNG。

## 动态纹理

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
  "animations": {
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

支持 `random_idle`、`loop`、`on_screen_open`、`on_totem_activate` 和 `manual` 触发器。

## 安全和限制

- ZIP 解压后最大 64 MiB、最多 2048 个文件。
- 禁止绝对路径、盘符路径和 `..` 路径。
- 骨骼最多 256 个、层级深度最多 64。
- 单个样式最多 64 个动作。
- 样式包不能执行 Java、JavaScript 或其他脚本。

