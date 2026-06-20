package lythro.hush.gui.anim;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks named 0..1 animation values that ease toward a target each frame. Lets a screen animate
 * many transient states (per-row hover, per-toggle position, …) without a field for each.
 *
 * <p>A key's value starts <em>at</em> its target the first time it's seen, so freshly shown
 * elements don't animate in from zero (the screen's own open animation handles entrance).
 */
public final class AnimTracker {

	private static final float DEFAULT_SPEED = 14.0f;

	private final Map<String, Float> values = new HashMap<>();

	/** Eases the named value toward 1 (active) or 0 (inactive) and returns the new value. */
	public float toward(final String key, final boolean active, final float dt) {
		return toward(key, active ? 1.0f : 0.0f, dt, DEFAULT_SPEED);
	}

	public float toward(final String key, final float target, final float dt, final float speed) {
		final float current = this.values.getOrDefault(key, target);
		final float next = Anim.approach(current, target, dt, speed);
		this.values.put(key, next);
		return next;
	}
}
