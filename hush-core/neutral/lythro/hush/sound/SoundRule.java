package lythro.hush.sound;

/**
 * How Hush should reshape a managed sound's playback. Derived from the user's
 * {@link lythro.hush.config.SoundSettings} and the global fade settings at play time.
 *
 * @param volume       final volume multiplier applied on top of the sound's own volume (1.0 = unchanged)
 * @param stereo       when true the sound is de-spatialised: played centred in both ears at a
 *                     constant volume instead of emitting directionally from its source position
 * @param cancelOnGone when true the sound is faded out once its source no longer exists
 * @param fadeMs       fade-out duration in milliseconds; 0 cuts the sound instantly
 * @param fadeCurve    shape of the fade-out
 */
public record SoundRule(float volume, boolean stereo, boolean cancelOnGone, int fadeMs, FadeCurve fadeCurve) {
}
