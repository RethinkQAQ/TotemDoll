# 导入、测试与排错

## 导入

1. 按 `F9` 打开 Totem Doll。
2. 点击左侧“导入”。
3. 选择“导入 ZIP”或“导入文件夹”。
4. 导入成功后返回模板页选择样式。

模组会复制原始包到：

```text
config/totemdoll/styles/imported/
```

文件夹导入同样会复制一份，因此修改原文件后需要重新导入。删除操作以整个包为单位。

## 常见错误

- `No style.json was found`：ZIP 根目录没有 `pack.json` 或 `style.json`。
- `Expected pack format 1`：`pack.json.format` 不为 `1`。
- `Expected style format 2`：`style.json.format` 不为 `2`。
- `Missing declared style`：`pack.json.styles` 指向了不存在的文件。
- `Path leaves the style pack`：内部路径包含 `..` 或尝试访问包外文件。
- `Missing texture/model`：文件名、大小写或相对路径不一致。
- `Unsupported model type`：当前只支持 `minecraft_item` 和 `minecraft_bone`。

详细错误会写入客户端日志：

```text
logs/latest.log
```

搜索 `Totem Doll`、`Could not compile imported style pack` 或 `Skipping invalid imported style`。

## 更新与删除

- 导入同名包时会生成独立目录，避免覆盖旧包。
- 确认新包正常后，可以在导入管理页删除旧包。
- 删除正在使用的包后应重新选择内置 Alex；如果没有可用样式则回退原版图腾。
- 导入和删除会触发一次 Minecraft 客户端资源重载。

