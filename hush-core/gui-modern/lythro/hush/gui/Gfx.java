package lythro.hush.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * {@link G} adapter for the 26.x retained-mode graphics class, {@code GuiGraphicsExtractor}.
 * Everything passes straight through; only the text methods carry the 26.x names
 * ({@code text} / {@code centeredText}). The transform calls map onto the 2D {@code Matrix3x2fStack}.
 */
public final class Gfx implements G {

	private final GuiGraphicsExtractor g;

	public Gfx(final GuiGraphicsExtractor g) {
		this.g = g;
	}

	@Override
	public void fill(final int x1, final int y1, final int x2, final int y2, final int color) {
		this.g.fill(x1, y1, x2, y2, color);
	}

	@Override
	public void fillGradient(final int x1, final int y1, final int x2, final int y2, final int top, final int bottom) {
		this.g.fillGradient(x1, y1, x2, y2, top, bottom);
	}

	@Override
	public void enableScissor(final int x1, final int y1, final int x2, final int y2) {
		this.g.enableScissor(x1, y1, x2, y2);
	}

	@Override
	public void disableScissor() {
		this.g.disableScissor();
	}

	@Override
	public void pushPose() {
		this.g.pose().pushMatrix();
	}

	@Override
	public void popPose() {
		this.g.pose().popMatrix();
	}

	@Override
	public void translate(final float dx, final float dy) {
		this.g.pose().translate(dx, dy);
	}

	@Override
	public void scale(final float s) {
		this.g.pose().scale(s);
	}

	@Override
	public void text(final Font font, final String text, final int x, final int y, final int color) {
		this.g.text(font, text, x, y, color);
	}

	@Override
	public void centeredText(final Font font, final String text, final int centerX, final int y, final int color) {
		this.g.centeredText(font, text, centerX, y, color);
	}
}
