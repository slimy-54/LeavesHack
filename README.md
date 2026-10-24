<h3 align="center">
  <img src="./src/main/resources/assets/leaves/icon.png" width="100" alt="Logo" style="border-radius: 15px;"/><br/>
  LeavesHack-Addon for <a href="https://meteorclient.com">Meteor Client</a>
</h3>

<p align="center">
  <a href="https://github.com/MrBZBZ/LeavesHack/stargazers"><img src="https://img.shields.io/github/stars/MrBZBZ/LeavesHack?colorA=363a4f&colorB=b7bdf8&style=for-the-badge" alt="Stars"></a>
  <a href="https://github.com/MrBZBZ/LeavesHack/issues"><img src="https://img.shields.io/github/issues/MrBZBZ/LeavesHack?colorA=363a4f&colorB=f5a97f&style=for-the-badge" alt="Issues"></a>
  <a href="https://github.com/MrBZBZ/LeavesHack/contributors"><img src="https://img.shields.io/github/contributors/MrBZBZ/LeavesHack?colorA=363a4f&colorB=a6da95&style=for-the-badge" alt="Contributors"></a>
  <a href="https://github.com/MrBZBZ/LeavesHack/releases"><img src="https://img.shields.io/github/downloads/MrBZBZ/LeavesHack/total?style=for-the-badge&labelColor=363a4f&color=f5c2e7" alt="Downloads"></a>
</p>

<p align="center">
  <i>致力于改善无政府服务器的游玩体验 / Dedicated to improving the anarchy server experience</i><br/>
  <i>适用 Grim 反作弊 / Designed for Grim Anti-Cheat</i>
</p>

---

## 语言 / Language

- [简体中文](#简体中文)
- [English](#english)

---

## 简体中文

LeavesHack 是面向 Meteor Client 的 Minecraft 1.21.11 客户端插件

### 环境要求

- Minecraft `1.21.11`
- Java `21` 或更高版本
- 从V2版本开始都是1.21.11，V1版本都是1.21.1
- [Meteor Client](https://meteorclient.com)
- 可选：[Litematica](https://modrinth.com/mod/litematica) 与 [MaLiLib](https://modrinth.com/mod/malilib)（Printer 使用）

### 安装与构建

1. 从 [Releases](https://github.com/MrBZBZ/LeavesHack/releases) 下载对应版本的 JAR。
2. 将 JAR 放入 Minecraft 的 `mods` 文件夹，并同时安装对应版本的 Meteor Client。
3. 启动游戏后，在 Meteor Client 的模块列表中查找 `LeavesCombat` 和 `LeavesMisc` 分类。

本地构建：

```powershell
.\gradlew.bat build
```

构建产物位于 `build/libs/`。

### 功能模块

以下列表以当前源码中实际注册的模块为准。

#### LeavesCombat

| 模块 | 功能 |
| --- | --- |
| `Aura` | 杀戮光环 |
| `AutoAnchor` | 自动重生锚 |
| `AutoArmorPlus` | 自动穿甲与鞘翅切换 |
| `AutoMine` | 自动挖掘 Burrow 和 Surround |
| `AutoOffHand` | 自动副手 |
| `AutoWeb` | 自动放置蛛网 |
| `CrystalAuraPlus` | 水晶战斗辅助 |
| `FireworkElytraFly` | 鞘翅烟花飞行 |
| `Follower` | 自动追人 |
| `GlassFiller` | 玻璃塞脚 |
| `PacketMine+` | 数据包挖掘 |
| `PushCrystal` | 推水晶 |
| `SelfTrapPlus` | 黑曜石自围 |

#### LeavesMisc

| 模块 | 功能 |
| --- | --- |
| `AutoLogin` | 自动登录 |
| `AutoPlaceBlock` | 自动铺方块 |
| `AutoRefreshTrade` | 自动刷新交易附魔书 |
| `AutoTorch` | 自动放火把 |
| `AutoTree` | 自动树场 |
| `BreakESP` | 挖掘进度显示 |
| `DeathRender` | 死亡渲染 |
| `FriendsManager` | 好友管理器 |
| `GlobalSetting` | 全局设置 |
| `ModuleList` | 模块列表渲染 |
| `Nuker+` | 方块破坏 |
| `PacketLogger` | 数据包记录 |
| `PlaceRender` | 方块放置渲染 |
| `Printer` | 投影打印机 |
| `Replenish` | 自动补充物品 |
| `Scaffold+` | 自动搭路 |
| `SprintPlus` | 自动疾跑 |

### 使用说明

- `GlobalSetting` 提供中文界面、转头、放置/攻击挥手、Baritone 接管等全局选项。
- `AutoLogin` 的账号数据由模块内的账号管理界面维护。
- `Printer` 依赖 Litematica 与 MaLiLib；未安装依赖时不要启用该模块。
- `FireworkElytraFly`、`ElytraGrimAccelerate` 和 `Scaffold+` 等模块涉及移动与飞行，具体效果取决于服务器和 Meteor Client 配置。

### 链接

- Bilibili：https://space.bilibili.com/1932462042
- 使用文档：https://leaveshack.netlify.app/
- 神秘群聊：`1076456572`

---

## English

LeavesHack is a Minecraft 1.21.11 client-side addon for Meteor Client.

### Requirements

- Minecraft `1.21.11`
- Java `21` or newer
- [Meteor Client](https://meteorclient.com)
- Optional: [Litematica](https://modrinth.com/mod/litematica) and [MaLiLib](https://modrinth.com/mod/malilib) for `Printer`

### Installation and build

1. Download the matching JAR from [Releases](https://github.com/MrBZBZ/LeavesHack/releases).
2. Put the JAR in Minecraft's `mods` folder and install the matching Meteor Client version.
3. After launching the game, find the modules under the `LeavesCombat` and `LeavesMisc` categories.

Build locally:

```powershell
.\gradlew.bat build
```

The build output is written to `build/libs/`.

### Modules

The following list matches the modules registered by the current source.

#### LeavesCombat

| Module | Description |
| --- | --- |
| `Aura` | KillAura |
| `AutoAnchor` | Automatic respawn-anchor combat |
| `AutoArmorPlus` | Automatic armor and elytra switching |
| `AutoMine` | Automatic Burrow and Surround mining |
| `AutoOffHand` | Automatic offhand management |
| `AutoWeb` | Automatic web placement |
| `CrystalAuraPlus` | Crystal combat assistance |
| `FireworkElytraFly` | Elytra firework flight |
| `Follower` | Automatic player following |
| `GlassFiller` | Glass foot trap |
| `PacketMine+` | Packet mining |
| `PushCrystal` | Crystal pushing |
| `SelfTrapPlus` | Obsidian self-trap |

#### LeavesMisc

| Module | Description |
| --- | --- |
| `AutoLogin` | Automatic login |
| `AutoPlaceBlock` | Automatic block placement |
| `AutoRefreshTrade` | Automatic villager trade refresh |
| `AutoTorch` | Automatic torch placement |
| `AutoTree` | Automatic tree farm |
| `BreakESP` | Mining progress display |
| `DeathRender` | Death rendering |
| `FriendsManager` | Friend manager |
| `GlobalSetting` | Global settings |
| `ModuleList` | Module list rendering |
| `Nuker+` | Block breaking |
| `PacketLogger` | Packet logging |
| `PlaceRender` | Block placement rendering |
| `Printer` | Litematica printer |
| `Replenish` | Automatic item replenishment |
| `Scaffold+` | Automatic scaffolding |
| `SprintPlus` | Automatic sprinting |

### Notes

- `GlobalSetting` includes Chinese UI, rotation, swing, Baritone integration, and other global options.
- `Printer` requires Litematica and MaLiLib. Do not enable it without those dependencies.
- The behavior of movement and flight modules depends on the server and Meteor Client configuration.

### Links

- Bilibili: https://space.bilibili.com/1932462042
- Documentation: https://leaveshack.netlify.app/

---

## Credits

- [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)
- [Meteor-Addon-Template](https://github.com/MeteorDevelopment/meteor-addon-template)
- [Meteor Translation Addon](https://github.com/Nippaku-Zanmu/meteor-translation-addon)
- [Baritone](https://github.com/cabaletta/baritone)
- [Epsilon](https://github.com/NekoyaHouse/Epsilon)
- [AntiAntiXray](https://github.com/tokfrans03/AntiAntiXray)

Special thanks:

- L3MonKe
- Chen Meng
- matl


