package lythro.hush.gui.theme;

/**
 * Small ARGB colour-maths helpers so the UI can derive gradients and strokes from a single base
 * colour instead of hand-picking every shade. Colours are {@code 0xAARRGGBB}.
 */
public final class Colors {

	private Colors() {
	}

	public static int alpha(final int c) {
		return (c >>> 24) & 0xFF;
	}

	public static int red(final int c) {
		return (c >> 16) & 0xFF;
	}

	public static int green(final int c) {
		return (c >> 8) & 0xFF;
	}

	public static int blue(final int c) {
		return c & 0xFF;
	}

	public static int argb(final int a, final int r, final int g, final int b) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	/** Blends {@code amount} (0..1) toward white, preserving alpha. */
	public static int lighten(final int c, final float amount) {
		return argb(alpha(c), lerpInt(red(c), 255, amount), lerpInt(green(c), 255, amount), lerpInt(blue(c), 255, amount));
	}

	/** Blends {@code amount} (0..1) toward black, preserving alpha. */
	public static int darken(final int c, final float amount) {
		return argb(alpha(c), lerpInt(red(c), 0, amount), lerpInt(green(c), 0, amount), lerpInt(blue(c), 0, amount));
	}

	/** Interpolates between two full ARGB colours. */
	public static int lerp(final int from, final int to, final float t) {
		final float c = Math.max(0.0f, Math.min(1.0f, t));
		return argb(
			lerpInt(alpha(from), alpha(to), c),
			lerpInt(red(from), red(to), c),
			lerpInt(green(from), green(to), c),
			lerpInt(blue(from), blue(to), c)
		);
	}

	/** Returns {@code c} with its alpha replaced. */
	public static int withAlpha(final int c, final int alpha) {
		return argb(alpha, red(c), green(c), blue(c));
	}

	private static int lerpInt(final int from, final int to, final float t) {
		return Math.round(from + (to - from) * t);
	}
}
