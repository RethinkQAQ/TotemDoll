# Totem Doll 样式包模板

这是 Totem Doll `format: 2` 样式包示例，可以作为 Blockbench 模型、网页编辑器和资源包作者的起始模板。

## 目录结构

```text
style-template/
├── pack.json
└── styles/
    └── example/
        ├── style.json
        ├── models/
        │   └── main.json
        └── textures/
            └── base.png
```

一个样式包可以在 `pack.json` 中声明多个 `style.json`。单个样式也可以省略 `pack.json`，直接导入包含 `style.json` 的文件夹。

## 编辑模型

使用 Blockbench 打开：

```text
styles/example/models/main.json
```

选择 Minecraft Java Item/Block 模型格式进行编辑。模型使用 `#skin` 纹理槽：

```json
{
  "textures": {
    "skin": "example:base"
  }
}
```

模型面的纹理引用也应使用：

```json
"texture": "#skin"
```

这样 `style.json` 中的 `skin` 定义就可以声明允许玩家导入 64×64 Minecraft 皮肤，并由模组替换这个纹理槽。

## style.json 要点

- `format` 必须为 `2`。
- `model.type` 当前使用 `minecraft_item`。
- `model.file` 是相对于当前样式目录的模型路径。
- `skin.supported` 控制是否可以从该模板创建个人样式。
- `features.animations` 和 `features.dynamic_textures` 声明动作或动态纹理能力。
- 不要在样式包中使用绝对路径或包含 `..` 的路径。

当前运行时支持 Minecraft 原版 Item JSON、Totem Doll 骨骼模型、骨骼动作和动态纹理。骨骼示例位于 `styles/animated_example/`。

