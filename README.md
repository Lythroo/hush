<div align="center">
  <img src="docs/icon.png" width="120" alt="Hush icon">

  # Hush

  **Quiets Minecraft's harshest sounds, and fades them out when their source is gone.**

  [![License: LGPL v3](https://img.shields.io/badge/license-LGPL--3.0-blue.svg)](LICENSE)
  &nbsp;·&nbsp; Fabric &amp; NeoForge &nbsp;·&nbsp; MC 1.21.1 · 1.21.11 · 26.1 · 26.2
</div>

---

Hush is a lightweight, **client-side** Minecraft mod that tames the few sounds that are needlessly
loud, jarring, or never seem to stop, without muting the game. It reshapes how a handful of intrusive
sounds are *heard*, and smoothly fades a sound out when the entity that made it dies or despawns.

## Features

- **Softens the enderman.** Quiets the scream and de-spatialises the long, block-anchored "stare"
  drone (toggle: stereo or 3D).
- **Fades dying sounds.** A mob's lingering sounds fade out when it dies or despawns; its death sound
  is always kept.
- **Tames the nether portal.** The portal entry sound fades when you step away.
- **Tunable fade.** Set the fade-out duration and curve.
- **Clean in-game config.** One simple settings page, opened from a keybind or (on Fabric) Mod Menu.

## Download

Grab the jar matching your loader and Minecraft version from the [Releases](../../releases) page or
from Modrinth.

| Minecraft | Fabric | NeoForge |
|---|:---:|:---:|
| 1.21.1 | ✅ | ✅ |
| 1.21.11 | ✅ | ✅ |
| 26.1 (26.1.x) | ✅ | ✅ |
| 26.2 | ✅ | ✅ |

**Fabric API** is required on Fabric. **Mod Menu** is optional (never bundled). Without it, bind the
config key under *Options > Controls > Hush*. On NeoForge, use the config button in the mods list. The
mod is client-side only, so it never needs to be installed on a server.

## Repository layout

A multi-version monorepo. **All shared logic lives in [`hush-core/`](hush-core)** and is pulled into
each per-loader/version build via Gradle `srcDir` (no Architectury), so the whole mod is written once.

```
hush-core/                 shared, loader- and version-agnostic code
  neutral/                 all logic: sound-engine mixin, config, the GUI (drawn against a `G` interface)
  gui-modern/              26.x adapter        (GuiGraphicsExtractor, MouseButtonEvent)
  gui-legacy/              retained-mode 1.21.8+ adapter  (GuiGraphics, 2D Matrix3x2fStack)
  gui-classic/             immediate-mode 1.21.1 adapter  (GuiGraphics, 3D PoseStack, classic mouse)

hush-<mc>/                 Fabric build for that Minecraft version
hush-<mc>-NeoForge/        NeoForge build for that Minecraft version
```

Each `hush-<mc>[-NeoForge]` folder is a thin, independent Gradle project (entry points, mod metadata,
build config). The handful of things that differ between loaders or versions sit behind one seam,
`HushPlatform` (config dir, screen opening, sound-id accessor). Full architecture notes:
[`hush-core/NEXT_SESSION.md`](hush-core/NEXT_SESSION.md).

## Building

Each target folder builds on its own. You need:

- **JDK 21** for the `1.21.x` targets, **JDK 25** for the `26.x` targets.

```bash
cd hush-1.21.1            # or any hush-* target folder
JAVA_HOME=/path/to/jdk ./gradlew build      # jar lands in build/libs/
```

> The Fabric `1.21.x` targets use the `net.fabricmc.fabric-loom-remap` plugin, because 1.21.11 is the
> last **obfuscated** Minecraft version whereas 26.x ships unobfuscated.

## License

[GNU Lesser General Public License v3.0](LICENSE). The LGPL builds on the GPL, whose full text is in
[`COPYING`](COPYING).
