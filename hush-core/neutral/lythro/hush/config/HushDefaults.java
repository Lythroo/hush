package lythro.hush.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lythro.hush.sound.source.SourceKind;

/**
 * The sounds Hush manages out of the box and the order categories appear in the UI.
 *
 * <p>This is the single place to add new managed sounds &mdash; vanilla or modded. Each entry is
 * seeded into a fresh config and merged into existing ones without clobbering the user's choices
 * (see {@link #apply}). To support a mod, add its sound ids here with a sensible category; the rest
 * of the pipeline already handles arbitrary namespaces.
 *
 * <p>Defaults only ever <em>add</em> behaviour that fits the mod's purpose: the curated mob sounds
 * below merely fade out when their source dies (volume/timbre unchanged). Only the enderman is
 * quietened/de-spatialised, since that was specifically requested. Death and hurt sounds are
 * deliberately excluded so a mob's death sound is never cut.
 */
public final class HushDefaults {

	private record Default(
		String id, String category, float volume, boolean stereo, boolean cancelOnGone, SourceKind sourceKind
	) {
	}

	/** The enderman's long, block-anchored "stare" drone — surfaced as the "de-spatialise" UI toggle. */
	public static final String ENDERMAN_STARE = "minecraft:entity.enderman.stare";
	/** The nether-portal entry sound — surfaced as the "tame portal" UI toggle. */
	public static final String PORTAL_TRIGGER = "minecraft:block.portal.trigger";

	private static final List<Default> DEFAULTS = List.of(
		// Enderman — the originally requested case: quieter, and the long block-anchored "stare"
		// is de-spatialised. Both fade out when the enderman is gone.
		new Default("minecraft:entity.enderman.scream", "Sounds", 0.45F, false, true, SourceKind.ENTITY),
		new Default(ENDERMAN_STARE, "Sounds", 0.45F, true, true, SourceKind.ENTITY),

		// Most mobs are now handled by the blanket "silence dying entities" rule
		// (HushConfig.silenceDyingEntities), so they don't need individual entries here. Only sounds
		// that need *special* treatment, or whose source isn't a living entity, are listed.

		// Primed TNT fuse — not a living entity, so the blanket rule skips it; fade it explicitly
		// when the TNT entity is gone (it exploded).
		new Default("minecraft:entity.tnt.primed", "Sounds", 1.0F, false, true, SourceKind.ENTITY),

		// Nether portal — the rising trigger sound fades out when you step out of the portal.
		new Default(PORTAL_TRIGGER, "Sounds", 1.0F, false, true, SourceKind.PORTAL)
	);

	/**
	 * Sounds for which the stereo (de-spatialise) toggle is meaningful and shown in the UI. Stereo
	 * only makes sense for long, position-anchored sounds; for everything else the toggle is hidden
	 * and the engine ignores any stored stereo flag (see {@code HushConfig.ruleFor}).
	 */
	private static final Set<String> STEREO_CAPABLE = Set.of(
		"minecraft:entity.enderman.stare"
	);

	private static final Map<String, SourceKind> SOURCE_KINDS = buildSourceKinds();

	private HushDefaults() {
	}

	/** Seeds any missing default entries into the given map, leaving existing entries as-is. */
	public static void apply(final Map<String, SoundSettings> sounds) {
		for (final Default d : DEFAULTS) {
			sounds.putIfAbsent(d.id(), new SoundSettings(true, d.volume(), d.stereo(), d.cancelOnGone(), d.category()));
		}
	}

	/** Whether the given sound supports the stereo / de-spatialise toggle. */
	public static boolean allowsStereo(final String id) {
		return STEREO_CAPABLE.contains(id);
	}

	/** How the given sound's source should be tracked for cancel-on-loss. Defaults to entity. */
	public static SourceKind sourceKind(final String id) {
		return SOURCE_KINDS.getOrDefault(id, SourceKind.ENTITY);
	}

	private static Map<String, SourceKind> buildSourceKinds() {
		final Map<String, SourceKind> map = new HashMap<>();
		for (final Default d : DEFAULTS) {
			if (d.sourceKind() != SourceKind.ENTITY) {
				map.put(d.id(), d.sourceKind());
			}
		}
		return Map.copyOf(map);
	}
}
