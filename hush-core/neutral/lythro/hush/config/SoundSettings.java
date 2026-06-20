package lythro.hush.config;

/**
 * Per-sound settings, stored in {@link HushConfig} and serialised to {@code config/hush.json}.
 *
 * <p>Plain mutable public fields keep this compatible with the Gson version bundled in Minecraft
 * and let the config UI edit it in place. Works for any sound id (vanilla or modded); nothing here
 * is hardcoded to a specific namespace.
 *
 * @see SoundRule the immutable transform derived from these settings at play time
 */
public final class SoundSettings {

	/** When false, Hush leaves this sound untouched (plays exactly like vanilla). */
	public boolean enabled = true;

	/** Final volume multiplier applied on top of the sound's own volume (1.0 = unchanged). */
	public float volume = 1.0F;

	/** When true the sound is de-spatialised: played centred in both ears instead of directionally. */
	public boolean stereo = false;

	/** When true the sound is faded out once its source (the mob that played it) is gone. */
	public boolean cancelOnGone = false;

	/** UI grouping only (tab the row appears under); has no effect on playback. */
	public String category = "Mobs";

	public SoundSettings() {
	}

	public SoundSettings(
		final boolean enabled, final float volume, final boolean stereo,
		final boolean cancelOnGone, final String category
	) {
		this.enabled = enabled;
		this.volume = volume;
		this.stereo = stereo;
		this.cancelOnGone = cancelOnGone;
		this.category = category;
	}
}
