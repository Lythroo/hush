package lythro.hush.gui.anim;

/**
 * Frame-rate-independent animation helpers.
 *
 * <p>{@link #approach} uses exponential smoothing so a value eases toward a target at the same
 * real-time rate regardless of FPS &mdash; the right tool for hover/toggle/slide animations driven
 * by a per-frame delta time.
 */
public final class Anim {

	private Anim() {
	}

	/**
	 * Eases {@code current} toward {@code target} by a fraction determined by {@code dt} (seconds)
	 * and {@code speed} (higher = snappier). Frame-rate independent.
	 */
	public static float approach(final float current, final float target, final float dt, final float speed) {
		if (dt <= 0.0f) {
			return current;
		}
		final float t = 1.0f - (float) Math.exp(-speed * dt);
		return current + (target - current) * t;
	}

	public static float clamp01(final float v) {
		return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
	}

	public static float easeOutCubic(final float t) {
		final float u = 1.0f - clamp01(t);
		return 1.0f - u * u * u;
	}
}
