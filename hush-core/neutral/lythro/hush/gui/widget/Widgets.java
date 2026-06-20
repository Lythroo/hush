package lythro.hush.gui.widget;

import lythro.hush.gui.Rect;
import lythro.hush.gui.theme.Colors;
import lythro.hush.gui.theme.HushTheme;
import lythro.hush.gui.theme.HushTheme.ButtonStyle;
import net.minecraft.client.gui.Font;
import lythro.hush.gui.G;

/**
 * Stateless, reusable drawing primitives for Hush's UI.
 *
 * <p>Each method takes an explicit {@link Rect} so it can be reused anywhere without owning layout
 * state. Corners are softened by omitting the four corner pixels. Surfaces use a subtle top-lighter
 * vertical gradient plus a 1px stroke (both derived from a single base colour via {@link Colors})
 * so everything has a little depth and definition instead of looking flat.
 */
public final class Widgets {

	private static final int TEXT_HEIGHT = 8;
	private static final float GRADIENT = 0.04f;
	private static final float STROKE_LIFT = 0.24f;

	private Widgets() {
	}

	/** Fills a rectangle with 1px-rounded corners (flat colour). */
	public static void roundedRect(final G g, final Rect r, final int color) {
		g.fill(r.x() + 1, r.y(), r.right() - 1, r.bottom(), color);
		g.fill(r.x(), r.y() + 1, r.right(), r.bottom() - 1, color);
	}

	/** Fills a rectangle with a vertical gradient and rounded corners. */
	public static void roundedGradient(final G g, final Rect r, final int top, final int bottom) {
		g.fillGradient(r.x() + 1, r.y(), r.right() - 1, r.bottom(), top, bottom);
		g.fillGradient(r.x(), r.y() + 1, r.right(), r.bottom() - 1, top, bottom);
	}

	/** Draws a 1px stroke that follows the rounded corners. */
	public static void stroke(final G g, final Rect r, final int color) {
		g.fill(r.x() + 1, r.y(), r.right() - 1, r.y() + 1, color);            // top
		g.fill(r.x() + 1, r.bottom() - 1, r.right() - 1, r.bottom(), color);  // bottom
		g.fill(r.x(), r.y() + 1, r.x() + 1, r.bottom() - 1, color);           // left
		g.fill(r.right() - 1, r.y() + 1, r.right(), r.bottom() - 1, color);   // right
	}

	/** A surface: gradient fill derived from {@code base}, with a subtly lighter stroke. */
	public static void surface(final G g, final Rect r, final int base) {
		surface(g, r, base, Colors.lighten(base, STROKE_LIFT));
	}

	/** A surface with an explicit stroke colour. */
	public static void surface(final G g, final Rect r, final int base, final int strokeColor) {
		roundedGradient(g, r, Colors.lighten(base, GRADIENT), Colors.darken(base, GRADIENT));
		stroke(g, r, strokeColor);
	}

	/** Draws a button whose background blends toward its hover colour by {@code hover} (0..1). */
	public static void button(
		final G g, final Font font, final Rect r, final String label,
		final ButtonStyle style, final float hover
	) {
		final int base = Colors.lerp(style.bg(), style.bgHover(), hover);
		surface(g, r, base, style.border());
		textCentered(g, font, label, r, style.text());
	}

	/**
	 * Draws an on/off toggle pill. {@code progress} (0..1) slides the knob and blends the track
	 * colour, so animating it gives a smooth on/off transition.
	 */
	public static void toggle(final G g, final Rect r, final float progress) {
		final int base = Colors.lerp(HushTheme.TRACK, HushTheme.ACCENT, progress);
		surface(g, r, base, Colors.lighten(base, STROKE_LIFT));
		final int knobSize = r.h() - 4;
		final int minX = r.x() + 2;
		final int maxX = r.right() - 2 - knobSize;
		final int knobX = Math.round(minX + (maxX - minX) * progress);
		final Rect knob = new Rect(knobX, r.y() + 2, knobSize, knobSize);
		roundedGradient(g, knob, Colors.lighten(HushTheme.KNOB, 0.10f), Colors.darken(HushTheme.KNOB, 0.10f));
	}

	/** Draws a horizontal slider with a filled track and a draggable knob. {@code progress} is 0..1. */
	public static void slider(final G g, final Rect r, final float progress, final float hover) {
		final int trackH = 4;
		final int trackY = r.centerY() - trackH / 2;
		roundedRect(g, new Rect(r.x(), trackY, r.w(), trackH), HushTheme.TRACK);
		final int fillW = Math.round((r.w()) * progress);
		if (fillW > 0) {
			roundedRect(g, new Rect(r.x(), trackY, fillW, trackH), HushTheme.ACCENT);
		}
		final int knobSize = 12;
		final int knobX = r.x() + Math.round((r.w() - knobSize) * progress);
		final Rect knob = new Rect(knobX, r.centerY() - knobSize / 2, knobSize, knobSize);
		final int base = Colors.lerp(HushTheme.KNOB, HushTheme.ACCENT, hover * 0.35f);
		roundedGradient(g, knob, Colors.lighten(base, 0.10f), Colors.darken(base, 0.10f));
		stroke(g, knob, Colors.darken(base, 0.22f));
	}

	/** Draws left-aligned text vertically centred within {@code bounds}, starting at {@code x}. */
	public static void textLeft(
		final G g, final Font font, final String text, final int x, final Rect bounds, final int color
	) {
		g.text(font, text, x, textY(bounds), color);
	}

	/** Draws horizontally and vertically centred text within {@code bounds}. */
	public static void textCentered(
		final G g, final Font font, final String text, final Rect bounds, final int color
	) {
		g.centeredText(font, text, bounds.centerX(), textY(bounds), color);
	}

	private static int textY(final Rect bounds) {
		return bounds.y() + (bounds.h() - TEXT_HEIGHT) / 2;
	}
}
