# 本地构建

本仓库仅发布源码，不提供安装包，也不设置自动上传 APK 的公开工作流。

## 环境

- JDK 17。
- Android SDK Platform 34、Build Tools 34.0.0。
- 仓库自带的 Gradle Wrapper；首次构建需要联网下载依赖。

将 Android SDK 路径配置到 `ANDROID_HOME`，或在本地 `local.properties` 写入 `sdk.dir`。不要提交本地 SDK 路径、账号凭据或签名私钥。

## 命令

Linux / macOS：

```sh
bash ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleRelease
```

Windows PowerShell：

```powershell
.\gradlew.bat --no-daemon :app:testDebugUnitTest :app:assembleRelease
```

本地 APK 输出位于 `app/build/outputs/apk/release/`；该目录和 APK 文件不应提交。Release 使用上游已有的代码精简、混淆与资源裁剪规则，Debug 包通常明显更大。

## 安装标识与签名

修复版包名为 `com.huanchengfly.tieba.post.localfix`，可以与上游原版共存。服务类名仍是 `com.huanchengfly.tieba.post.services.OKSignService`，不能通过安装包名拼接服务类名。

未配置自有签名时，项目使用当前构建环境的 Android 调试签名。不同机器或临时构建环境产生的签名可能不同，不能保证覆盖安装。自行维护和备份签名密钥；不要把密钥或密码提交到本仓库。

## 验证范围

自动化测试覆盖回复目标校验与回复、删除的状态更新；真实账号的回复、删除、签到结果仍需在设备上验证。设备上的通知权限会影响签到进度通知是否可见。

本仓库具备从源码自行构建的条件，但未承诺不同环境产出逐字节一致的 APK。
