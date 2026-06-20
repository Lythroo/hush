package lythro.hush.sound.source;

/**
 * Tracks whether the thing that produced a sound still exists, so Hush can fade the sound out once
 * its source is gone.
 *
 * <p>Implementations are intentionally tiny and one-directional: they answer {@link #isAlive()} and
 * nothing else. Phase 3 ships {@link EntitySourceTracker}; block- and radius-based trackers can be
 * added later without touching the sound wrapper.
 */
public interface SoundSourceTracker {

	/** Whether the source still exists. Once this turns false, Hush begins fading the sound. */
	boolean isAlive();
}
