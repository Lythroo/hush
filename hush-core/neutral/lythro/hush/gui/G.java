package lythro.hush.gui;

import net.minecraft.client.gui.Font;

/**
 * Minimal drawing surface the shared GUI is written against, so the same widgets and config screen
 * work across Minecraft's two GUI-graphics APIs.
 *
 * <p>The graphics class differs by version &mdash; {@code GuiGraphicsExtractor} on 26.x, plain
 * {@code GuiGraphics} on the 1.21.x line &mdash; and even the text-drawing methods are named
 * differently ({@code text}/{@code centeredText} vs {@code drawString}/{@code drawCenteredString}).
 * Each era supplies a tiny {@code Gfx} adapter implementing this interface; everything in
 * {@link lythro.hush.gui.widget.Widgets} and {@link HushConfigScreenBase} then draw
 * through {@code G} and never names a version-specific type.
 *
 * <p>The transform stack is exposed as plain operations rather than a raw matrix object because that
 * object diverges too: a 2D {@code Matrix3x2fStack} on the retained-mode line (1.21.8+ and 26.x) but
 * a 3D {@code PoseStack} on immediate-mode 1.21.1. Each {@code Gfx} maps these four calls onto its
 * own stack ({@code pushMatrix}/{@code translate(x,y)}/{@code scale(s)} vs
 * {@code pushPose}/{@code translate(x,y,0)}/{@code scale(s,s,1)}).
 */
public interface G {

	void fill(int x1, int y1, int x2, int y2, int color);

	void fillGradient(int x1, int y1, int x2, int y2, int top, int bottom);

	void enableScissor(int x1, int y1, int x2, int y2);

	void disableScissor();

	/** Save the current transform. */
	void pushPose();

	/** Restore the transform saved by the matching {@link #pushPose()}. */
	void popPose();

	/** Translate subsequent drawing by {@code (dx, dy)}. */
	void translate(float dx, float dy);

	/** Uniformly scale subsequent drawing about the current origin. */
	void scale(float s);

	/** Left/top-anchored text at {@code (x, y)}. */
	void text(Font font, String text, int x, int y, int color);

	/** Text horizontally centred on {@code centerX} at {@code y}. */
	void centeredText(Font font, String text, int centerX, int y, int color);
}
