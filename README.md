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
