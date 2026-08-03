# Totem Doll 样式包模板

这是 Totem Doll `format: 3` 跨版本 mesh 样式包示例，可以作为 Blockbench、网页编辑器和样式包作者的起始模板。

## 目录结构

```text
style-template/
├── pack.json
└── styles/
    └── example/
        ├── style.json
        ├── models/
        │   └── geometry.json
        └── textures/
            └── base.png
```

一个样式包可以在 `pack.json` 中声明多个 `style.json`。单个样式也可以省略 `pack.json`，直接导入包含 `style.json` 的文件夹。

## 编辑模型

使用 Blockbench 打开：

```text
styles/example/models/geometry.json
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

- `format` 必须为 `3`。
- `model.type` 固定使用 `mesh`。
- `model.geometry` 是相对于当前样式目录的 geometry 路径。
- `skin.supported` 控制是否可以从该模板创建个人样式。
- `features.animations` 和 `features.dynamic_textures` 声明动作或动态纹理能力。
- `texture_animations` 只描述纹理帧动画；顶层 `animations` 只描述骨骼动作触发绑定。
- 不要在样式包中使用绝对路径或包含 `..` 的路径。

运行时只读取 TotemDoll `format:3` mesh 格式，不直接读取 Minecraft 原版 Item Model JSON。Blockbench 的 Java Item/Block 格式只作为创作输入，发布前必须转换为 `geometry.json`。骨骼动画示例位于 `styles/animated_example/`。

