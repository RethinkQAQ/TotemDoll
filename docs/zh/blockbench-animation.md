# Blockbench 骨骼动作工作流

Totem Doll 不直接读取 `.bbmodel`。Blockbench 用于编辑骨骼和关键帧，发布时导出统一的 `geometry.json`、`animations.json` 和 `style.json`。

## 推荐骨骼

```text
root
└── body
    ├── head
    ├── left_arm
    ├── right_arm
    ├── left_leg
    └── right_leg
```

骨骼名称必须与 `animations.json` 中的名称完全一致。pivot、立方体 origin/size 和 UV 使用 Blockbench 模型单位；旋转关键帧使用角度，position 使用模型单位，scale 使用倍率。

## 导出文件

- `geometry.json`：骨骼层级、pivot、立方体和 UV。
- `animations.json`：rotation、position、scale 时间轴。
- `style.json`：模型类型、纹理和触发规则。
- `textures/base.png`：模型纹理。

第一版插值支持 `linear`、`step`、`smooth`；触发器支持 `loop`、`random_idle`、`on_screen_open`、`on_totem_activate` 和 `manual`。

完整可运行示例位于 `style-template/styles/animated_example/`。

动作样式同样可以声明玩家皮肤替换。将 `skin.supported` 设为 `true`，
格式设为 `minecraft_64x64`，并令 `skin.target` 指向 `textures.base` 使用的
纹理槽。模组创建个人样式时会复制几何与动画文件，仅替换该基础纹理。

