package lythro.hush.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * {@link G} adapter for immediate-mode 1.21.1 (pre-1.21.8), whose graphics class is {@code GuiGraphics}
 * but whose {@code pose()} is still the 3D {@code PoseStack} rather than the retained-mode
 * {@code Matrix3x2fStack}. The four transform ops therefore map onto {@code pushPose}/{@code popPose}
 * and the 3-component {@code translate}/{@code scale} (z left at 0/1). Text uses the older
 * {@code drawString} / {@code drawCenteredString} names, exactly like {@code gui-legacy}.
 */
public final class Gfx implements G {

	private final GuiGraphics g;

	public Gfx(final GuiGraphics g) {
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
		this.g.pose().pushPose();
	}

	@Override
	public void popPose() {
		this.g.pose().popPose();
	}

	@Override
	public void translate(final float dx, final float dy) {
		this.g.pose().translate(dx, dy, 0.0f);
	}

	@Override
	public void scale(final float s) {
		this.g.pose().scale(s, s, 1.0f);
	}

	@Override
	public void text(final Font font, final String text, final int x, final int y, final int color) {
		this.g.drawString(font, text, x, y, color);
	}

	@Override
	public void centeredText(final Font font, final String text, final int centerX, final int y, final int color) {
		this.g.drawCenteredString(font, text, centerX, y, color);
	}
}
