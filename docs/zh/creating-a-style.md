# 制作 Totem Doll 样式

## 从模板开始

复制仓库中的 `style-template/`，然后修改：

1. `pack.json` 的包 ID、名称和作者。
2. `style.json` 的样式 ID 和名称。
3. `models/` 中的模型。
4. `textures/` 中的 PNG。

ID 建议使用自己的命名空间，例如 `author_name:my_doll`。

## 静态 Mesh 模型

使用 Blockbench 创建 Minecraft Java Item/Block 模型，并通过转换工具导出为 `models/geometry.json`。静态模型与动画模型使用同一套 mesh 格式，只是没有动画文件。

`style.json` 中使用逻辑纹理槽：

```json
{
  "textures": {
    "base": "textures/base.png"
  }
}
```

运行时会把包内相对纹理解析为当前样式的资源路径。

## 骨骼和动作模型

Blockbench 的 `.bbmodel` 是编辑源文件，游戏不会直接读取。使用：

```powershell
.\tools\convert-bbmodel.ps1 -InputFile .\model.bbmodel -OutputDirectory .\styles\my_style -StyleId author:my_style
```

转换后检查 `models/geometry.json`、`models/animations.json` 和 `style.json`。详细骨骼与触发规则见 [Blockbench 动画工作流](blockbench-animation.md)。

## 发布

ZIP 的第一层应直接是 `pack.json` 或 `style.json`。模组也接受外层只有一个同名文件夹的常见 ZIP。

发布前至少测试：

- 物品栏和配置界面预览。
- 第一、第三人称手持。
- 掉落物和展示框。
- 图腾激活动画。
- 透明纹理和第二层皮肤。
- Fabric 与 NeoForge。

