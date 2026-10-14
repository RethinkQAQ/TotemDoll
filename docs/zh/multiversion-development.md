# 版本开发

Totem Doll 使用 Stonecutter 0.9.8，维护 Fabric、NeoForge 和共享 common 节点。构建结构同步自 `RethinkQAQ/multi-loader-mod-template` 的 `ecb0273f490cb65ff31805f4fc5cc3b16bd33eab`，保留 RCUI 注解处理和项目专用打包逻辑。

根目录 `gradle.properties` 中的 `stonecutter_enabled_versions` 决定当前注册的版本，各 `versions/<version>/gradle.properties` 中的 `enable_platforms` 决定该版本启用的 loader。未注册或未启用的组合不会构建、收集或生成启动配置。

```text
common/
fabric/
neoforge/
versions/
├── 1.21.1/
│   └── gradle.properties
└── 1.21.3/
    └── gradle.properties
```

校验和构建目标：

```powershell
.\gradlew.bat validateVersionProperties
.\gradlew.bat build
.\gradlew.bat collectJars
.\gradlew.bat licenseCheck
.\gradlew.bat :fabric:1.21.1:build
.\gradlew.bat :fabric:26.3:build
.\gradlew.bat :neoforge:1.21.1:build
.\gradlew.bat :fabric:26.3:printRunJava
```

`collectJars` 将本次构建的正式 JAR 汇集到 `build/mod-jars/`，不会包含旧版本号、停用组合或开发 JAR。普通 Actions 构建使用 `mod.version` 并追加 `-build.<run_number>`；Release 使用 tag 中的版本号。

新增版本时，复制 `versions/<version>/gradle.properties`，设置 `minecraft_version`、`enable_platforms`、`publish_version`、兼容范围、`java.version` 和依赖，然后将版本加入注册列表。`resource_pack_format` 使用原版 JAR 的 `version.json` 中的资源包格式；新格式写成主次版本，如 `97.1`。Java 21 用于 `1.21.x`，Java 25 用于 `26.x`。

执行 `.\gradlew.bat stonecutterIdea` 并刷新 Gradle 后，IDE 会生成对应版本的 Gradle 启动项。默认 `run.client=true`、`run.server=false`；Fabric 继续使用模块内的 `runs/client`，NeoForge 保留其原有运行目录。通过 `printRunJava` 可以确认实际启动 JVM。

RCUI 包装类由 common 的注解处理器生成，再由 loader 编译和打包。不要移除公共 Java 产物中声明的注解处理输出，或让编译前的协议头格式任务依赖这些编译输出，否则可能遗漏类或造成循环依赖。
