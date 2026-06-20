package lythro.hush.gui;

/**
 * An immutable integer rectangle with the geometry helpers the GUI needs.
 *
 * <p>Widgets are drawn and hit-tested against {@code Rect}s so layout (which positions them) is
 * cleanly separated from rendering (which doesn't care where it is). Reusable across any screen.
 */
public record Rect(int x, int y, int w, int h) {

	public int right() {
		return x + w;
	}

	public int bottom() {
		return y + h;
	}

	public int centerX() {
		return x + w / 2;
	}

	public int centerY() {
		return y + h / 2;
	}

	public boolean contains(final double px, final double py) {
		return px >= x && px < right() && py >= y && py < bottom();
	}

	/** Returns this rectangle shrunk by {@code dx} on each horizontal edge and {@code dy} vertical. */
	public Rect inset(final int dx, final int dy) {
		return new Rect(x + dx, y + dy, w - 2 * dx, h - 2 * dy);
	}
}
