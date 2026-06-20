package lythro.hush.sound;

import java.util.concurrent.CompletableFuture;
import lythro.hush.sound.source.SoundSourceTracker;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

/**
 * Wraps another {@link SoundInstance} and reshapes how it is played without changing what is played.
 *
 * <p>Everything is delegated to the wrapped instance so resolution, audio streams and subtitles
 * behave exactly like vanilla. On top of that it applies a {@link SoundRule}:
 * <ul>
 *   <li><b>volume</b> &ndash; scales the final volume by a multiplier.</li>
 *   <li><b>stereo</b> &ndash; removes 3D positioning so the sound plays centred in both ears.</li>
 *   <li><b>cancel-on-source-lost</b> &ndash; when a {@link SoundSourceTracker} is attached and its
 *       source disappears, the sound fades out over {@code fadeMs} and then stops.</li>
 * </ul>
 *
 * <p>It is a {@link TickableSoundInstance} so the engine ticks it and re-reads its volume every
 * tick (enabling the fade). For sounds without a tracker this is harmless: it never stops itself
 * early and ends naturally like any one-shot. The wrapper is tagged by type so the engine mixin can
 * skip re-processing it.
 */
public final class HushSoundInstance implements TickableSoundInstance {

	private final SoundInstance delegate;
	private final SoundRule rule;
	private final @Nullable SoundSourceTracker source;

	// Cached once (after resolve) so a per-tick getVolume()/getPitch() doesn't re-sample ranged values.
	private float baseVolume = -1.0f;
	private float basePitch = -1.0f;

	private boolean stopped;
	private long fadeStartMs = -1L;

	public HushSoundInstance(final SoundInstance delegate, final SoundRule rule, final @Nullable SoundSourceTracker source) {
		this.delegate = delegate;
		this.rule = rule;
		this.source = source;
	}

	// --- lifecycle (TickableSoundInstance) ---

	@Override
	public void tick() {
		if (this.stopped) {
			return;
		}
		final long now = System.currentTimeMillis();
		// Begin fading the moment the source is gone (dead or despawned).
		if (this.source != null && this.fadeStartMs < 0 && !this.source.isAlive()) {
			if (this.rule.fadeMs() <= 0) {
				this.stopped = true;
				return;
			}
			this.fadeStartMs = now;
		}
		if (this.fadeStartMs >= 0 && now - this.fadeStartMs >= this.rule.fadeMs()) {
			this.stopped = true;
		}
	}

	@Override
	public boolean isStopped() {
		return this.stopped;
	}

	private float fadeFactor() {
		if (this.fadeStartMs < 0) {
			return 1.0f;
		}
		final int duration = this.rule.fadeMs();
		final long elapsed = System.currentTimeMillis() - this.fadeStartMs;
		if (duration <= 0 || elapsed >= duration) {
			return 0.0f;
		}
		return this.rule.fadeCurve().remaining((float) elapsed / duration);
	}

	// --- transformed behaviour ---

	@Override
	public float getVolume() {
		if (this.baseVolume < 0.0f && this.delegate.getSound() != null) {
			this.baseVolume = this.delegate.getVolume();
		}
		final float base = this.baseVolume < 0.0f ? 0.0f : this.baseVolume;
		final float scaled = base * this.rule.volume();
		if (this.fadeStartMs < 0L) {
			// Not fading: leave loudness/range untouched. Vanilla uses volume > 1 to extend audible
			// range and the engine clamps the actual gain to 1, so this matches vanilla behaviour.
			return scaled;
		}
		// While fading, clamp to the audible maximum *before* applying the fade. Otherwise the
		// engine's [0,1] gain clamp masks the fade for loud sounds (the ghast plays at volume 5–10),
		// so the curve only becomes audible in its last few percent — i.e. an abrupt cut.
		return Math.min(scaled, 1.0f) * fadeFactor();
	}

	@Override
	public float getPitch() {
		if (this.basePitch < 0.0f && this.delegate.getSound() != null) {
			this.basePitch = this.delegate.getPitch();
		}
		return this.basePitch < 0.0f ? this.delegate.getPitch() : this.basePitch;
	}

	@Override
	public Attenuation getAttenuation() {
		return this.rule.stereo() ? Attenuation.NONE : this.delegate.getAttenuation();
	}

	@Override
	public boolean isRelative() {
		return this.rule.stereo() || this.delegate.isRelative();
	}

	@Override
	public double getX() {
		return this.rule.stereo() ? 0.0 : this.delegate.getX();
	}

	@Override
	public double getY() {
		return this.rule.stereo() ? 0.0 : this.delegate.getY();
	}

	@Override
	public double getZ() {
		return this.rule.stereo() ? 0.0 : this.delegate.getZ();
	}

	// --- pass-through ---

	@Override
	public Identifier getIdentifier() {
		return this.delegate.getIdentifier();
	}

	@Override
	public @Nullable WeighedSoundEvents resolve(final SoundManager soundManager) {
		return this.delegate.resolve(soundManager);
	}

	@Override
	public @Nullable Sound getSound() {
		return this.delegate.getSound();
	}

	@Override
	public SoundSource getSource() {
		return this.delegate.getSource();
	}

	@Override
	public boolean isLooping() {
		return this.delegate.isLooping();
	}

	@Override
	public int getDelay() {
		return this.delegate.getDelay();
	}

	@Override
	public boolean canStartSilent() {
		return this.delegate.canStartSilent();
	}

	@Override
	public boolean canPlaySound() {
		return this.delegate.canPlaySound();
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(
		final SoundBufferLibrary soundBuffers, final Identifier id, final boolean repeatInstantly
	) {
		return this.delegate.getAudioStream(soundBuffers, id, repeatInstantly);
	}
}
