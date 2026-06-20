package lythro.hush.mixin;

import lythro.hush.HushPlatform;
import lythro.hush.config.HushConfig;
import lythro.hush.config.HushDefaults;
import lythro.hush.sound.HushSoundInstance;
import lythro.hush.sound.SoundRule;
import lythro.hush.sound.source.EntitySourceTracker;
import lythro.hush.sound.source.SoundSourceTracker;
import lythro.hush.sound.source.SoundSources;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Reshapes managed sounds at the single point every sound passes through.
 *
 * <p>{@code play} reads attenuation, relativity, position and volume straight off the
 * {@link SoundInstance}, so swapping the argument for a {@link HushSoundInstance} wrapper at
 * HEAD is enough to change how the sound is heard. This runs before {@code resolve()}, which is
 * exactly why we wrap (and delegate) rather than rebuild the instance.
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {

	@ModifyVariable(method = "play", at = @At("HEAD"), argsOnly = true)
	private SoundInstance hush$reshape(final SoundInstance instance) {
		// The engine re-enters play() for queued sounds; never wrap our own wrapper twice.
		if (instance instanceof HushSoundInstance) {
			return instance;
		}

		final HushConfig cfg = HushConfig.get();
		// The id accessor differs by version (getLocation/getIdentifier) so the seam hands us the String.
		final String id = HushPlatform.soundId(instance);

		// 1) A specific, enabled per-sound rule takes precedence (e.g. the enderman's volume/stereo).
		final SoundRule rule = cfg.ruleFor(id);
		if (rule != null) {
			final @Nullable SoundSourceTracker source =
				rule.cancelOnGone() ? SoundSources.resolve(instance, HushDefaults.sourceKind(id)) : null;
			return new HushSoundInstance(instance, rule, source);
		}

		// Don't apply the blanket rule when Hush is off, or when this sound is explicitly configured
		// (its rule was null because the user disabled it).
		if (!cfg.enabled || cfg.sounds.containsKey(id)) {
			return instance;
		}

		// 2) Blanket rule: fade a living entity's sound (except its death sound) when it dies or
		// despawns — unless the user excluded that mob.
		if (cfg.silenceDyingEntities && isDyingEntitySound(id)) {
			final Entity source = SoundSources.nearestLiving(instance);
			if (source != null && cfg.isMobSilenced(EntityType.getKey(source.getType()).toString())) {
				final SoundRule blanket = new SoundRule(1.0f, false, true, cfg.fade.durationMs, cfg.fade.curve);
				return new HushSoundInstance(instance, blanket, new EntitySourceTracker(source));
			}
		}
		return instance;
	}

	/** An entity sound that should stop when the entity dies — i.e. anything but its death sound. */
	private static boolean isDyingEntitySound(final String id) {
		// Path is the part after the namespace, e.g. "minecraft:entity.zombie.ambient" → "entity.zombie.ambient".
		final int colon = id.indexOf(':');
		final String path = colon < 0 ? id : id.substring(colon + 1);
		return path.startsWith("entity.") && !path.endsWith(".death");
	}
}
