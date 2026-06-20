package lythro.hush.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lythro.hush.HushPlatform;
import lythro.hush.sound.FadeCurve;
import lythro.hush.sound.SoundRule;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * User-editable configuration, persisted as {@code config/hush.json}.
 *
 * <p>Holds a {@link SoundSettings} per specially-handled sound (keyed by full sound id string, e.g.
 * {@code minecraft:entity.enderman.scream}) plus the per-mob {@link #mobsExcluded} set. The sound
 * engine mixin calls {@link #ruleFor} for every sound played; a {@code null} result means "leave it
 * to vanilla".
 *
 * <p>This class is part of the cross-loader / cross-version core: it keys everything on
 * {@code String} (never {@code Identifier}/{@code ResourceLocation}) and reads its directory from
 * {@link HushPlatform}, so it compiles unchanged on every target. Defaults live in
 * {@link HushDefaults}.
 */
public final class HushConfig {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static @Nullable HushConfig instance;

	/** Master switch; when false Hush leaves every sound untouched. */
	public boolean enabled = true;

	/**
	 * Blanket rule: fade out any living entity's sounds when it dies (except its death sound),
	 * even sounds not individually listed below. Catches zombie groans, skeleton rattles, etc.
	 */
	public boolean silenceDyingEntities = true;

	/** Global fade-out settings, shared by every cancel-on-source sound. */
	public FadeConfig fade = new FadeConfig();

	/**
	 * Mobs the user has opted OUT of the blanket silencing, by entity-type id (e.g.
	 * {@code minecraft:zombie}). Stored as exceptions, so the default (empty) silences every mob.
	 */
	public Set<String> mobsExcluded = new HashSet<>();

	/** Managed sounds, keyed by full sound id. Insertion order is preserved for the UI. */
	public Map<String, SoundSettings> sounds = new LinkedHashMap<>();

	/** Whether the given mob (entity-type id) should have its sounds silenced when it dies. */
	public boolean isMobSilenced(final String entityTypeId) {
		return !this.mobsExcluded.contains(entityTypeId);
	}

	// --- named sound toggles surfaced individually in the config UI ---
	// These read/write specific entries of the `sounds` map (seeded by HushDefaults). Guarded against a
	// missing entry in case a user pruned it from hush.json by hand.

	/** Whether the enderman's "stare" drone is de-spatialised (played centred) rather than positional 3D. */
	public boolean endermanStereo() {
		final SoundSettings s = this.sounds.get(HushDefaults.ENDERMAN_STARE);
		return s != null && s.stereo;
	}

	/** Sets the enderman stereo/3D state. */
	public void setEndermanStereo(final boolean stereo) {
		final SoundSettings s = this.sounds.get(HushDefaults.ENDERMAN_STARE);
		if (s != null) {
			s.stereo = stereo;
		}
	}

	/** Whether Hush manages (fades out) the nether-portal entry sound. */
	public boolean portalEnabled() {
		final SoundSettings s = this.sounds.get(HushDefaults.PORTAL_TRIGGER);
		return s != null && s.enabled;
	}

	/** Sets whether Hush manages the nether-portal entry sound. */
	public void setPortalEnabled(final boolean enabled) {
		final SoundSettings s = this.sounds.get(HushDefaults.PORTAL_TRIGGER);
		if (s != null) {
			s.enabled = enabled;
		}
	}

	/** How sounds fade when their source is lost. */
	public static final class FadeConfig {
		/** Fade-out duration in milliseconds; 0 cuts instantly. */
		public int durationMs = 400;
		/** Shape of the fade. */
		public FadeCurve curve = FadeCurve.SMOOTH;
	}

	private static Path path() {
		return HushPlatform.configDir().resolve("hush.json");
	}

	/** Returns the loaded config, loading it from disk on first access. */
	public static HushConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	/** (Re)loads the config from disk, applying defaults for any missing entries, then saves. */
	public static void load() {
		final Path path = path();
		HushConfig loaded = null;
		if (Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				loaded = GSON.fromJson(reader, HushConfig.class);
			} catch (Exception e) {
				LOGGER.error("[Hush] Failed to read {}, using defaults", path, e);
			}
		}
		if (loaded == null) {
			loaded = new HushConfig();
		}
		if (loaded.sounds == null) {
			loaded.sounds = new LinkedHashMap<>();
		}
		if (loaded.fade == null) {
			loaded.fade = new FadeConfig();
		}
		if (loaded.mobsExcluded == null) {
			loaded.mobsExcluded = new HashSet<>();
		}
		HushDefaults.apply(loaded.sounds);
		instance = loaded;
		save();
	}

	/** Writes the current config to disk, pretty-printed. */
	public static void save() {
		if (instance == null) {
			return;
		}
		final Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (var writer = Files.newBufferedWriter(path)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LOGGER.error("[Hush] Failed to write {}", path, e);
		}
	}

	/**
	 * The rule to apply to the given sound id, or {@code null} if Hush should not touch it. Takes a
	 * plain {@code String} (e.g. {@code "minecraft:entity.enderman.scream"}) so the core stays free of
	 * version-specific id types; callers pass {@code id.toString()}.
	 */
	public @Nullable SoundRule ruleFor(final String key) {
		if (!enabled) {
			return null;
		}
		final SoundSettings settings = sounds.get(key);
		if (settings == null || !settings.enabled) {
			return null;
		}
		final boolean stereo = settings.stereo && HushDefaults.allowsStereo(key);
		return new SoundRule(settings.volume, stereo, settings.cancelOnGone, this.fade.durationMs, this.fade.curve);
	}
}
