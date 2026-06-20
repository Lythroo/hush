# Hush — multi-version handoff (resume here)

Client-side MC mod that fades/softens sounds when their source dies/despawns. Goal: ship
**Fabric + NeoForge** across **4 MC versions** spanning **two GUI eras**, from ONE shared codebase.

## Architecture (DONE, proven)
Shared **3-tier core** at `D:\Mods\hush-core\`, consumed by each per-loader/version project via Gradle
`srcDir` (NO Architectury — its loom can't do 26.x). Canonical package `lythro.hush` everywhere.

- `hush-core/neutral/` — everything shared: all sound/config/source logic, the **mixin**
  (`SoundEngineMixin`), `HushMobs`, `HushSounds`, theme/anim/Rect, the **`G` graphics interface**,
  `Widgets`, `TabBar`, and abstract **`HushConfigScreenBase`** (all GUI logic, written against `G`).
- `hush-core/gui-modern/` — 26.x only: `Gfx` wraps `GuiGraphicsExtractor`; `HushConfigScreen` overrides
  `extractRenderState(...)`.
- `hush-core/gui-legacy/` — retained-mode 1.21.8+ (1.21.11): `Gfx` wraps `GuiGraphics` (its `pose()` is
  already the 2D `Matrix3x2fStack`); `HushConfigScreen` overrides `render(GuiGraphics,...)` + no-op
  `renderBackground`.
- `hush-core/gui-classic/` — immediate-mode 1.21.1 (pre-1.21.8): `Gfx` wraps `GuiGraphics` but its
  `pose()` is the 3D `PoseStack`, and the mouse callbacks use the classic
  `mouseClicked(double,double,int)` signatures (no `MouseButtonEvent`).

**Graphics adapter** = the key trick: GUI written once against `G`; each era supplies a ~70-line
`Gfx implements G`. `G` exposes drawing (fill/fillGradient/scissor/text/centeredText) AND the
transform stack as plain ops (`pushPose`/`popPose`/`translate(x,y)`/`scale(s)`) — NOT a raw matrix
object, because that object diverges (`Matrix3x2fStack` retained vs `PoseStack` classic). Per-era
`HushConfigScreen` subclass calls `draw(new Gfx(g), mx, my, () -> super.<nativeRender>(g,mx,my,a))`
and owns the native mouse overrides, forwarding to the base's primitive `hushMouse*(mx,my,button)`
helpers (the `MouseButtonEvent` vs `(double,double,int)` split lives only in the subclass).

**Seams** (in `neutral/lythro/hush/HushPlatform.java`): `configDir()` (Fabric `FabricLoader` vs
NeoForge `FMLPaths`); `openScreen(Screen)` (26.2 `setScreenAndShow`/`Gui.setScreen` vs 26.1+1.21.x
classic `Minecraft.setScreen`); and `soundId(SoundInstance)` (1.21.1 `getLocation()`/`ResourceLocation`
vs 1.21.11+/26.x `getIdentifier()`/`Identifier`, reduced to a `String`). Each client init calls
`setConfigDir(...)` + `setScreenOpener(...)` + `setSoundIdExtractor(...)`.

**Per-loader, NOT shared:** entry/keybind glue + `sound/HushSoundInstance` (Fabric `getAudioStream`
vs NeoForge `getStream`) + mixin-config json + mod metadata.

GUI-ERA FACT: retained-mode landed in MC **1.21.8**, so **1.21.11 is retained** but still uses the
plain `GuiGraphics` class + `render(...)` (the `GuiGraphicsExtractor`/`extractRenderState` rename is
26.x-only). **1.21.1 is immediate-mode** (pre-1.21.8) — PROBED: it differs on three axes (3D
`PoseStack` not `Matrix3x2fStack`; classic mouse signatures not `MouseButtonEvent`; sound id
`getLocation()`/`ResourceLocation` not `getIdentifier()`/`Identifier`; key category is a String not
`KeyMapping.Category`). All four are now seamed (gui-classic tier + `soundId` seam); NeoForge builds.

## Target matrix (8 of 8 building + 8 of 8 running 🎉)
All targets reach "Sound engine started" with the SoundEngineMixin applied and no mixin/FATAL errors.
| Project folder | MC | Loader | Build | Ran in-game |
|---|---|---|---|---|
| `hush-26.2` | 26.2 | Fabric | ✅ | ✅ |
| `hush-26.2-NeoForge` | 26.2 | NeoForge | ✅ | ✅ |
| `hush-26.1` | 26.1.2 | Fabric | ✅ | ✅ |
| `hush-26.1-NeoForge` | 26.1.2 | NeoForge | ✅ | ✅ |
| `hush-1.21.11-NeoForge` | 1.21.11 | NeoForge | ✅ | ✅ (loads, mixin applies) |
| `hush-1.21.11` | 1.21.11 | Fabric | ✅ (loom-remap) | ✅ (loads, mixin applies) |
| `hush-1.21.1-NeoForge` | 1.21.1 | NeoForge | ✅ (gui-classic) | ✅ (loads, mixin applies) |
| `hush-1.21.1` | 1.21.1 | Fabric | ✅ (gui-classic + loom-remap) | ✅ (loads, mixin applies) |

## Version pins (confirmed)
- 26.2: neo `26.2.0.3-beta`; Fabric loom `1.17-SNAPSHOT`, fabric-api `0.152.1+26.2`, modmenu `20.0.0-beta.2`. Java 25.
- 26.1.2: neo `26.1.2.71`; fabric-api `0.152.1+26.1.2`, modmenu `18.0.0-beta.1`. Java 25.
- 1.21.11: neo `21.11.42`; fabric-api `0.141.3+1.21.11`, modmenu `17.0.0`. Java 21.
- 1.21.1: neo `21.1.233`; fabric-api TBD (`0.x+1.21.1`), modmenu TBD, architectury-api `13.0.8` (if ever needed). Java 21.

## Build commands
JDK paths: Java 25 = `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`, Java 21 = `...jdk-21.0.10.7-hotspot`.
```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.3.9-hotspot"   # 26.x targets
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot"  # 1.21.x targets
cd /d/Mods/<project> && ./gradlew build --console=plain
# Fabric also has compileClientJava; NeoForge does not.
```
Stop a dev client: PowerShell — kill java/javaw whose CommandLine matches the project folder name.

## TODO (priority order)

### A. Fabric obfuscated-Loom blocker — SOLVED (2026-06-19). Both Fabric 1.21.x build.
The error `"Cannot use Mojang mappings in a non-obfuscated environment"` was NEVER a Loom *version*
problem — it was the wrong **plugin id**. Since Loom 1.14, the same loom artifact ships two plugin ids:
`net.fabricmc.fabric-loom` (assumes an already-unobfuscated game → 26.x) and
**`net.fabricmc.fabric-loom-remap`** (obfuscated → 1.21.x). 1.21.11 is the LAST obfuscated MC (26.1 is
the first unobfuscated). FIX in each Fabric 1.21.x build.gradle:
```
plugins { id 'net.fabricmc.fabric-loom-remap' version "${loom_version}" ... }
```
Keep loom_version `1.16-SNAPSHOT` (resolves to Loom 1.16.3), gradle 9.4.1, loader 0.19.3,
`mappings loom.officialMojangMappings()`. Confirmed against `fabric-example-mod` branch `1.21.11`.
Two follow-on fixes were needed and are done:
- Mod deps must be `modImplementation` (not `implementation`) so Loom remaps them.
- fabric-api 0.141.x (1.21.11) / 0.116.x (1.21.1) expose the keybind helper as the pre-rename
  `keybinding.v1.KeyBindingHelper#registerKeyBinding` — the `keymapping.v1.KeyMappingHelper` used by
  26.x doesn't exist yet (HushKeybinds per-loader files updated).
- Fabric 1.21.1 only: MC 1.21.1 doesn't bundle JSpecify (1.21.11+/26.x do), so add
  `clientCompileOnly "org.jspecify:jspecify:1.0.0"` + `mavenCentral()`. (NeoForge bundles JSpecify, so
  its 1.21.1 didn't need this.)

NO refmap needed: modern Loom remaps mixin refs in-place to intermediary at remapJar
(verified `SoundEngineMixin.class` → `class_1140` etc., no named refs). `hush-1.21.1` (Fabric) was
created this session by cloning `hush-1.21.11` (Fabric) + repinning + applying the 1.21.1 seams
(gui-classic srcDir, getLocation extractor, String key category, getLocation in HushSoundInstance).

### B. 1.21.1 NeoForge — DONE (builds). Fabric 1.21.1 — remaining.
`hush-1.21.1-NeoForge` now **builds** (`./gradlew build`, Java 21 → `build/libs/hush-1.0.0.jar`). The
probe found 1.21.1 diverges on four axes vs 1.21.11, all now seamed (see GUI-ERA FACT + Seams above):
- **pose**: 3D `PoseStack`, not `Matrix3x2fStack` → new **`gui-classic`** tier. `G` was refactored so
  `pose()` (returning a concrete matrix) became plain ops `pushPose/popPose/translate/scale`; every
  `Gfx` (modern/legacy/classic) + the two neutral callers updated.
- **mouse**: classic `mouseClicked(double,double,int)` etc., no `MouseButtonEvent` → native overrides
  moved OUT of `HushConfigScreenBase` into each per-era `HushConfigScreen`, forwarding to new protected
  `hushMouseClicked/Dragged/Released` helpers. (`mouseScrolled`'s 4-arg signature matches all targets,
  stays a neutral override.)
- **sound id**: `getLocation()`/`ResourceLocation` → new `HushPlatform.soundId` seam; mixin no longer
  names `Identifier`. `HushSoundInstance` (per-loader) overrides `getLocation()`.
- **key category**: `KeyMapping.Category` doesn't exist → `HushKeyMappings` passes the lang-key String
  `"key.category.hush.hush"` directly.

All 7 client inits now call `setSoundIdExtractor(...)` (1.21.1 → `getLocation`, rest → `getIdentifier`).
Regression-verified: 26.2-NeoForge (gui-modern) + 1.21.11-NeoForge (gui-legacy) still compile.

REMAINING for B: nothing build-wise — Fabric 1.21.1 created & builds (see TODO A). Runtime smoke-test
still pending (TODO C).

### Cleanup done (2026-06-19): removed fabric-example-mod template cruft
All 4 Fabric targets carried a leftover no-op `ExampleMixin` + `hush.mixins.json` (the template's
example). On the Java-21 targets (1.21.x) that config declared `compatibilityLevel: JAVA_25` — a latent
runtime crash (Mixin rejects a compat level above the running JVM; `"required": true`). Deleted
`ExampleMixin.java` + `hush.mixins.json` and dropped the `hush.mixins.json` entry from every Fabric
`fabric.mod.json`. All 4 Fabric targets now have an identical client-only mixin set (just
`hush.client.mixins.json` → `SoundEngineMixin`). NeoForge targets never had this cruft.

### C. Verify — ALL 8 BUILD; none runtime-confirmed yet
Runtime smoke-test each target (mixin applies → no mixin failure in log; open config via
keybind/mod-menu; the `soundId` seam throws on the FIRST sound if a client init missed
`setSoundIdExtractor` — watch for it). **Priority: the two `gui-classic` targets**
(`hush-1.21.1-NeoForge`, `hush-1.21.1`) — they exercise brand-new PoseStack rendering + classic mouse
input; confirm the config screen actually draws and clicks, not just that it launches. Also unrun:
`hush-26.1` (Fabric), `hush-1.21.11-NeoForge`, `hush-1.21.11` (Fabric). Per the user's goal, spot-check
that the config screen looks/behaves identically across a 26.x, a 1.21.11, and a 1.21.1 build.

## Notes
- Full project history/decisions are in the Claude memory `hush-mod.md` (may not travel across PCs —
  this file is the portable copy).
- NeoForge side is smooth (MDG handles every MC version). All pain is Fabric-Loom-for-obfuscated-MC.
- Don't guess loom/gradle versions — pin from an official template that targets the exact MC version.
- NeoForge 1.21.1 IDE-sync gotcha (fixed): the `data` run must call `data()`, NOT `clientData()` —
  21.1.x predates the split client/server data runs (error: `unknown run: clientData`). 1.21.11/26.x
  NeoForge support `clientData()`; only the 1.21.1 NeoForge clone needed downgrading to `data()`.
- NeoForge 1.21.1 launch gotcha (fixed): older FML (4.0.x, MC 1.21.1) REQUIRES `modLoader="javafml"` +
  `loaderVersion="[4,)"` at the top of `neoforge.mods.toml`, else it crashes at load with
  `InvalidModFileException: Missing ModLoader in file`. The shared template omitted them (1.21.11+/26.x
  made them optional, so 26.x ran fine without). Added to BOTH 1.21.x NeoForge templates; 26.x left
  as-is (proven). The dev-run mod-folder grouping (classes+resources) was never the problem.
