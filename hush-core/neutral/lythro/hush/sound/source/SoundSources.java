package lythro.hush.sound.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the source of a played sound so its lifecycle can be tracked.
 *
 * <p>The sound system only hands us a position, not the entity that played the sound, so we match
 * the nearest entity to that position. Mobs emit their sounds from their own location, so a small
 * search radius reliably finds the right one. Resolution happens at play time, when the entity is
 * still where the sound came from.
 */
public final class SoundSources {

	/** How close (to its bounding box) an entity must be to the sound to be considered its source. */
	private static final double RADIUS = 1.5;

	private SoundSources() {
	}

	/** Resolves a tracker for the given source kind, or {@code null} if none could be determined. */
	public static @Nullable SoundSourceTracker resolve(final SoundInstance instance, final SourceKind kind) {
		return switch (kind) {
			case ENTITY -> resolveEntity(instance);
			case PORTAL -> new PredicateSourceTracker(SoundSources::isPlayerInPortal);
		};
	}

	/** Whether the local player is currently standing in a nether-portal block. */
	public static boolean isPlayerInPortal() {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		final ClientLevel level = mc.level;
		if (player == null || level == null) {
			return false;
		}
		final AABB bb = player.getBoundingBox();
		final BlockPos min = BlockPos.containing(bb.minX + 0.001, bb.minY + 0.001, bb.minZ + 0.001);
		final BlockPos max = BlockPos.containing(bb.maxX - 0.001, bb.maxY - 0.001, bb.maxZ - 0.001);
		for (final BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds the entity that most likely emitted {@code instance}, or {@code null} if none is close
	 * enough (e.g. a block- or UI-played sound). Uses the instance's real position, so it works even
	 * for sounds Hush will de-spatialise.
	 */
	public static @Nullable SoundSourceTracker resolveEntity(final SoundInstance instance) {
		final Entity e = nearest(instance, x -> true);
		return e == null ? null : new EntitySourceTracker(e);
	}

	/** The nearest living entity (mob/player) to the sound, or {@code null}. Lets callers inspect its type. */
	public static @Nullable Entity nearestLiving(final SoundInstance instance) {
		return nearest(instance, e -> e instanceof net.minecraft.world.entity.LivingEntity);
	}

	private static @Nullable Entity nearest(final SoundInstance instance, final java.util.function.Predicate<Entity> filter) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		if (level == null) {
			return null;
		}
		final double x = instance.getX();
		final double y = instance.getY();
		final double z = instance.getZ();
		// Relative sounds (UI/music) carry no world position; nothing to track.
		if (x == 0.0 && y == 0.0 && z == 0.0) {
			return null;
		}
		final Vec3 pos = new Vec3(x, y, z);

		try {
			// Search generously, then filter by true distance to each entity's bounding box. Mobs
			// emit sounds from feet, eye or centre height, so matching the box (not a single point)
			// reliably finds tall sources like the enderman.
			final AABB box = AABB.ofSize(pos, RADIUS * 2 + 2, RADIUS * 2 + 6, RADIUS * 2 + 2);
			Entity nearest = null;
			double nearestSq = RADIUS * RADIUS;
			for (final Entity e : level.getEntities((Entity) null, box)) {
				if (e == mc.player || !filter.test(e)) {
					continue;
				}
				final double d = distanceToBoxSq(e.getBoundingBox(), x, y, z);
				if (d <= nearestSq) {
					nearestSq = d;
					nearest = e;
				}
			}
			return nearest;
		} catch (RuntimeException e) {
			// Defensive: never let source resolution break sound playback.
			return null;
		}
	}

	/** Squared distance from a point to the nearest point on a bounding box (0 if inside). */
	private static double distanceToBoxSq(final AABB box, final double x, final double y, final double z) {
		final double dx = Math.max(0.0, Math.max(box.minX - x, x - box.maxX));
		final double dy = Math.max(0.0, Math.max(box.minY - y, y - box.maxY));
		final double dz = Math.max(0.0, Math.max(box.minZ - z, z - box.maxZ));
		return dx * dx + dy * dy + dz * dz;
	}
}

