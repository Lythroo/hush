package lythro.hush.sound.source;

import java.lang.ref.WeakReference;
import net.minecraft.world.entity.Entity;

/**
 * Tracks a sound's source entity. The entity is held weakly so a tracked sound never keeps a
 * removed entity from being garbage-collected.
 *
 * <p>{@code isAlive()} becomes false the moment the entity dies (health hits zero) or is removed
 * from the world &mdash; that's the cue for the sound to fade out.
 */
public final class EntitySourceTracker implements SoundSourceTracker {

	private final WeakReference<Entity> entity;

	public EntitySourceTracker(final Entity entity) {
		this.entity = new WeakReference<>(entity);
	}

	@Override
	public boolean isAlive() {
		final Entity e = this.entity.get();
		return e != null && e.isAlive();
	}
}
