package lythro.hush.sound.source;

import java.util.function.BooleanSupplier;

/**
 * A {@link SoundSourceTracker} backed by an arbitrary liveness check. Used for sources that aren't
 * a single entity &mdash; e.g. "is the player still standing in a portal?".
 */
public final class PredicateSourceTracker implements SoundSourceTracker {

	private final BooleanSupplier alive;

	public PredicateSourceTracker(final BooleanSupplier alive) {
		this.alive = alive;
	}

	@Override
	public boolean isAlive() {
		return this.alive.getAsBoolean();
	}
}
