# Tieba Lite 修复源码

基于 [HuanCheng65/TiebaLite](https://github.com/HuanCheng65/TiebaLite) 的非官方修复分支，基线为 `4.0.0-beta.1` / `2885b2aabbbf47aba7bf12b1cd7cbc03b1f5ec15`。

**本项目仅供学习、研究与技术交流。请勿将本修复分支用于商业运营、收费代签或付费分发；这是维护者的用途倡议，不是对 GPL-3.0 增加的许可限制。**

## 仅发布源码

本仓库提供源代码、修改说明和本地构建方法，不提供 APK、AAB、安装包下载链接或公开构建产物，也不通过 Releases 发布安装包。需要使用的人请自行审阅源码、构建并签名。

本仓库不包含登录凭据、用户数据或签名私钥；不提供代登录、代签到或商业服务。

## 修改内容

修改日期：2026-10-24。

- 回复：优先使用已加载的帖子、楼层及楼中楼 ID，保留并传递验证参数，隔离编辑状态并展示发送错误。
- 删除：修复自己回复的归属判断，以及删除后列表与计数更新。
- 一键签到：使用实际的 `OKSignService` 类构建 Intent，修复增加 `.localfix` 安装包后缀后找不到后台服务的问题。
- 构建：修复版使用独立安装包名；Release 沿用上游代码精简和资源裁剪配置。

自动化测试不代表已完成真实账号的在线接口验证。

## 自行构建

详见 [BUILDING.md](BUILDING.md)。构建工具、依赖声明和 Gradle Wrapper 均保留在仓库中。

## 许可、来源与用途说明

保留上游 [GNU GPL v3 许可证全文](LICENSE)及原有版权标注；本分支的修改同样按 GPL-3.0 提供。原作者与本分支维护者的工作应当明确区分，不应声称本修复版本由原作者或百度官方提供或认可。

上游 README 的原始用途说明为：“本软件及源码仅供学习交流使用，严禁用于商业用途。”该原文作为来源说明保留。GPL-3.0 本身允许商业使用，不能通过本 README 的用途倡议取消许可证授予的权利；详见 [GNU 官方 FAQ](https://www.gnu.org/licenses/gpl-faq.html#NoMilitary)。

本项目按许可证规定以现状提供，不承诺接口持续可用或适合特定目的。使用者应遵守适用法律、平台规则及第三方权利；不得将本项目用于未获授权的数据访问、骚扰、垃圾信息或其他侵权行为。仅提供源码及学习用途说明并不等于免除依法应承担的责任。

## 致谢

- [HuanCheng65/TiebaLite](https://github.com/HuanCheng65/TiebaLite)
- [Starry-OvO/aiotieba](https://github.com/Starry-OvO/aiotieba)
- [n0099/tbclient.protobuf](https://github.com/n0099/tbclient.protobuf)
