# Hush — Plan & Architecture

**Hush** is a client-side Fabric mod for Minecraft 26.2 that tames intrusive sounds. It cancels or
fades a sound when its source is gone (a mob dies, you step out of a portal), and can soften
aggressive ones (de-spatialise + lower volume). Everything is configurable per sound.

## Positioning

A superset of the existing "Shut Up Dead Entities" (Fabric-only, no config). Hush's differentiators:

1. **Deep per-sound config** with an in-game UI (tabs, search, per-row controls).
2. **A general source-lifecycle engine** — entity death/despawn *and* listener-leaves-portal, built
   to extend to blocks/radius, not hardcoded special cases.
3. **Softening, not just silencing** — de-spatialise + volume per sound.
4. **Mod-friendliness** — nothing is hardcoded to `minecraft:`; any namespace works, and managed
   sounds live in one list (`HushDefaults`).
5. (Planned) **NeoForge** support alongside Fabric.

## How it works

Every sound passes through `SoundEngine#play(SoundInstance)`. A mixin (`@ModifyVariable` at HEAD)
looks the sound up in the config and, if managed, swaps it for a `HushSoundInstance` wrapper:

```
SoundEngine.play(sound) ──mixin──▶ rule = HushConfig.ruleFor(id)
                                       │ (null → leave to vanilla)
                                       ▼
                          HushSoundInstance(sound, rule, sourceTracker?)
                          • volume × multiplier
                          • stereo → de-spatialised (centred, no attenuation)
                          • tickable: if its source is gone, fade over fadeMs then stop
```

The wrapper is a `TickableSoundInstance`, so the engine re-reads its volume every tick — that's what
makes the fade possible. Source tracking is resolved at play time from the sound's *original*
position (so it works even for sounds we de-spatialise).

### Source tracking (`sound/source/`)

- `SoundSourceTracker` — `isAlive()`; once false, the sound fades.
- `EntitySourceTracker` — weak ref to the nearest entity whose **bounding box** is within 1.5 blocks
  of the sound (box-distance, because tall mobs emit from eye height).
- `PredicateSourceTracker` — arbitrary check; used for the portal ("is the player still in a portal
  block?").
- `SourceKind` + `SoundSources.resolve(instance, kind)` — picks the right tracker per sound.

## Layout

```
client/
  HushClient            entrypoint: load config + register keybinds
  HushKeybinds          "Open Hush Settings" under its own controls category
  config/
    HushConfig          load/save config/hush.json; ruleFor / category queries
    HushDefaults        ★ the managed-sound list + per-sound metadata (single place to extend)
    SoundSettings       per-sound data (enabled, volume, stereo, cancelOnGone, fadeMs, category)
  sound/
    SoundRule           immutable transform derived from settings at play time
    HushSoundInstance   tickable delegating wrapper (volume/stereo/fade)
    source/             SoundSourceTracker, EntitySourceTracker, PredicateSourceTracker,
                        SourceKind, SoundSources
  mixin/
    SoundEngineMixin    the single interception point
  gui/
    HushConfigScreen    tabs + search + per-sound rows + Done
    TabBar, Rect        layout + a category tab bar (sliding underline)
    HushSounds          UI click/toggle sounds
    widget/Widgets      reusable rounded surfaces, buttons, toggles
    theme/HushTheme, Colors   palette + colour maths
    anim/Anim, AnimTracker    frame-rate-independent easing
```

## Adding a managed sound (vanilla or modded)

Add one line to `HushDefaults.DEFAULTS`:

- Cancel-on-death only: `Default.cancelMob("namespace:entity.foo.bar")`
- Full control: `new Default(id, category, volume, stereo, cancelOnGone, sourceKind)`
- If it should show the stereo toggle, add its id to `STEREO_CAPABLE`.

## Status

| Phase | What | Status |
|------|------|--------|
| 0 | Project cleanup / metadata | ✅ |
| 1 | JSON config (`config/hush.json`) | ✅ |
| 2 | Enderman: de-spatialise + volume | ✅ |
| 3 | Cancel/fade on entity source loss | ✅ |
| 3b | Blanket "silence dying entities" (death + despawn, except death sound) | ✅ |
| 3c | Per-mob toggles for every mob, grouped into category tabs (Hostile/Passive/Water) | ✅ |
| 4 | Nether portal trigger (leave-portal fade) | ✅ |
| 5 | Config UI (tabs, search, rows, animations, sounds) | ✅ |
| 5b | Settings tab: master switch + fade duration slider + fade curve | ✅ |
| — | Per-sound cancel-on-death toggle in UI | ⬜ todo |
| — | Block/radius sources (beacon, conduit, jukebox) | ⬜ todo |
| 6 | Long global one-shots (wither spawn / dragon death) | ❌ dropped — would require touching sounds while the entity is alive / capping death sounds, which is against the design principle (only act on source removal) |
| — | NeoForge support — template at `D:\Mods\hush-26.2-NeoForge` (pkg `lythroo.hush`, neo 26.2.0.3-beta) | ⬜ todo |
| — | Mod Menu integration (v20.0.0-beta.2) | ✅ |

## Notes / caveats

- Existing `config/hush.json` files gain newly-added default sounds automatically (`putIfAbsent`),
  but pre-existing entries keep their stored values.
- `cancelOnGone` / `fadeMs` exist in config but have no UI control yet (file-editable).
- Death and hurt sounds are intentionally **not** managed, so they never get cut.
- Requires JDK 25 (toolchain pins it).
