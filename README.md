# 🌀 VoidPortals Add-on for BentoBox
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/VoidPortals)](https://ci.codemc.org/job/BentoBoxWorld/job/VoidPortals/)

## 🔍 What is VoidPortals?

**VoidPortals** is a BentoBox add-on that turns falling into the void into a way to travel between dimensions instead of a death sentence. When a player drops out of the bottom of the world, they are safely teleported to the matching location in the next dimension:

**Overworld → Nether → The End → Overworld**

It works with any BentoBox game mode (BSkyBlock, AcidIsland, CaveBlock, SkyGrid) and is controlled by a single per-world flag, so server admins decide exactly which worlds it applies to.

---

## 🚀 Getting Started

1. Place the **VoidPortals** `.jar` into your BentoBox `addons` folder.
2. Restart your server.
3. The flag is **disabled by default** — enable it per world to turn the feature on (see below).

---

## ⚙️ How it works

VoidPortals adds a single world setting flag, **Void world teleports** (`VOID_WORLD_TELEPORT_FLAG`, Ender Pearl icon).

To enable it, open the **Game Mode Admin Settings** panel and toggle the *Void world teleports* flag for the world you want. Once enabled, any player who falls into the void in that world is teleported with a portal effect to the next dimension:

- In the **Overworld**, they arrive in the **Nether**.
- In the **Nether**, they arrive in **The End**.
- In **The End**, they arrive back in the **Overworld**.

If the player is standing on an island, they are sent to that island's spawn point in the destination dimension; otherwise they arrive at the same X/Z coordinates in the destination world. Dead players, spectators, and players already being teleported are ignored.

---

## 🌍 Compatibility

- [x] BentoBox 3.14.0+
- [x] BSkyBlock
- [x] AcidIsland
- [x] CaveBlock
- [x] SkyGrid

---

## 🌐 Translations

Like most BentoBox projects, the VoidPortals Add-on is translatable into any language. Everyone can contribute and translate parts of the addon into their language via [GitLocalize](https://gitlocalize.com/repo/2975).
If your language is not in the list, please contact the developers via Discord and it will be added.

---

## 🔨 Building

VoidPortals builds with Maven and requires **Java 21**. It targets Paper 1.21.11 and BentoBox 3.14.0.

```bash
mvn clean package        # build the shaded jar into target/
mvn test                 # run the JUnit 5 / MockBukkit test suite
```

Nightly builds are available from the [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/VoidPortals/lastStableBuild/), and tagged releases from the [Releases tab](https://github.com/BentoBoxWorld/VoidPortals/releases).

---

## 🐛 Bugs and Feature Requests

Please submit issues at [GitHub Issues](https://github.com/BentoBoxWorld/VoidPortals/issues) or ask in the BentoBox [Discord](https://discord.bentobox.world). More information can be found in the [Wiki](https://github.com/BentoBoxWorld/VoidPortals/wiki).
