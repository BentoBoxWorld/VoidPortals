# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

VoidPortals is a **BentoBox addon** (not a standalone plugin). It lets players on any BentoBox GameMode (BSkyBlock, AcidIsland, CaveBlock, SkyGrid) teleport between dimensions by falling into the void instead of dying: Overworld → Nether → The End → Overworld. Behaviour is gated behind a per-world flag (`VOID_WORLD_TELEPORT_FLAG`) that is **disabled by default**.

The addon is small — the production code is three classes:
- `VoidPortalsAddon` — the `Addon` entry point (registers the flag with each GameMode + starts the listener)
- `VoidPortalsPladdon` — the `Pladdon` shim so the jar loads as a Bukkit/Paper plugin
- `listeners/VoidListener` — the gameplay logic

## Build & test

```bash
mvn clean package        # build the jar into target/
mvn test                 # run the JUnit 5 + MockBukkit suite
mvn test -Dtest=VoidListenerTest          # run a single test class
mvn test -Dtest=VoidListenerTest#testOverworldFallTeleportsToNether   # single method
```

- **Java 21**, Paper `1.21.11`, BentoBox `3.14.0-SNAPSHOT` (see `pom.xml` properties).
- Dependencies (BentoBox, Paper) are `provided` and resolved from the **PaperMC**, **CodeMC/BentoBoxWorld**, and **JitPack** repos, not Maven Central. The first build needs network access.
- The build produces `target/VoidPortals-<version>.jar`; copy it into a BentoBox server's `addons/` folder to test in-game. (All dependencies are `provided`/`test` scope, so there is nothing to shade — the jar is the plain addon classes plus resources.)
- Version is driven by the `build.version` property and Jenkins env vars (`BUILD_NUMBER`, `GIT_BRANCH`) via the `ci`/`master` profiles — `addon.yml` and `plugin.yml` use filtered `${...}` placeholders, so don't hardcode versions there.

## How the addon works (architecture)

**Lifecycle** (`VoidPortalsAddon.onEnable`): iterates every loaded GameMode addon and calls `VOID_WORLD_TELEPORT_FLAG.addGameModeAddon(...)` so the flag appears in each GameMode's admin settings, then registers the listener and the flag. If no GameMode is found, the addon logs an error and disables itself. The flag is a static `Flag.Builder` of `Type.WORLD_SETTING` with `defaultSetting(false)`.

**Teleport logic** (`VoidListener.onPlayerFallIntoVoid`): listens to `PlayerMoveEvent` at `EventPriority.LOWEST` with `ignoreCancelled = true`. When a player descends to `y ≤ 0` (falling into the void) in a GameMode world where the flag is set, it cancels the move and uses BentoBox's `SafeSpotTeleport.Builder(...).portal()` to send them to the next dimension. The destination is the island's spawn point for the target environment if an island exists at the fall location, otherwise the same x/z in the target world (resolved via the `GameModeAddon`'s `getNetherWorld()` / `getEndWorld()` / `getOverWorld()`). A `switch` on the current `world.getEnvironment()` selects the cycle direction (NORMAL→Nether, NETHER→End, THE_END→Overworld).

The early-return guard ladder at the top of the listener is the heart of the class — it filters out non-falling/upward movement, dead/spectator players, players already mid-teleport (`addon.getPlayers().isInTeleport(...)`), non-GameMode worlds, and worlds where the flag is off. Preserve its short-circuit ordering when editing; `VoidListenerTest` covers each guard.

## Testing conventions

Tests use **JUnit 5 + MockBukkit + Mockito** (no PowerMock). The pattern mirrors the BentoBox ecosystem (see `/Users/ben/git/CaveBlock` and `/Users/ben/git/DimensionalTrees` for the canonical sources):

- `CommonTestSetup` is the shared base: it mocks the `BentoBox` singleton (`WhiteBox.setInternalState`), the static `Bukkit`/`Util` classes, and the common managers. Subclasses call `super.setUp()`.
- `TestWorldSettings` is a minimal `WorldSettings`. **Its `getWorldFlags()` returns a persistent mutable `HashMap`** (unlike the CaveBlock original) — this is required so a test can enable `VOID_WORLD_TELEPORT_FLAG`, because `Flag.isSetForWorld` does a `computeIfAbsent` on that map.
- Listener happy-path tests stub `Util.getChunkAtAsync(...)` to a completed future so `SafeSpotTeleport.build()` doesn't kick off real async chunk scanning.
- Avoid fully-qualifying `world.bentobox...` names inside test classes that extend `CommonTestSetup` — the inherited `world` field shadows the `world` package segment. Import the type instead.

## Conventions

- Standard BentoBox Java style: 4-space indent, K&R braces, JavaDoc on public members. (The pre-modernisation code used Allman braces, tabs, and `this.` everywhere — that style is gone; match the current files.)
- **`CONTRIBUTING.md` is strict: formatting-only or automated-cleanup PRs are rejected.** Never reformat existing code as part of an unrelated change.
- User-facing strings live in `src/main/resources/locales/*.yml` keyed by flag name (e.g. `protection.flags.VOID_WORLD_TELEPORT_FLAG`). `en-US.yml` is the source of truth; other languages are translated via GitLocalize — don't hand-edit non-English locale files.
