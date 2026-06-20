package lythro.hush.sound.source;

/**
 * What kind of source a managed sound is tied to, so {@link SoundSources} knows how to track it.
 *
 * <p>Extensible: block-anchored sounds (beacon, conduit, …) can get their own kind later without
 * changing the wrapper or the mixin.
 */
public enum SourceKind {

	/** Tied to the entity that played it; fades when that entity dies or despawns. */
	ENTITY,

	/** The nether-portal trigger sound; fades when the player is no longer standing in a portal. */
	PORTAL
}
