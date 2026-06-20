package lythro.hush.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/**
 * Retained-mode 1.21.8+ entry point (e.g. 1.21.11) for the shared config screen.
 *
 * <p>This line draws screens via {@code render(GuiGraphics, …)}. We wrap that graphics object
 * in a {@link Gfx} adapter and delegate to {@link HushConfigScreenBase#draw}, passing a runnable
 * that invokes {@code super.render} (which draws the search box) inside the panel pose. The default
 * dirt/blur background is suppressed because the shared draw paints its own full-screen scrim.
 *
 * <p>Like 26.x, the mouse callbacks take a {@code MouseButtonEvent}; we unpack it and forward to the
 * shared primitive-typed {@code hushMouse*} handlers. (Immediate-mode 1.21.1 uses {@code gui-classic}.)
 */
public final class HushConfigScreen extends HushConfigScreenBase {

	public HushConfigScreen(final @Nullable Screen parent) {
		super(parent);
	}

	@Override
	public void render(final GuiGraphics g, final int mouseX, final int mouseY, final float a) {
		draw(new Gfx(g), mouseX, mouseY, () -> super.render(g, mouseX, mouseY, a));
	}

	@Override
	public void renderBackground(final GuiGraphics g, final int mouseX, final int mouseY, final float a) {
		// Intentionally empty: the shared draw() paints its own scrim, so skip the vanilla background.
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		return super.mouseClicked(event, doubleClick) || hushMouseClicked(event.x(), event.y(), event.button());
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
		return hushMouseDragged(event.x()) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(final MouseButtonEvent event) {
		return hushMouseReleased() || super.mouseReleased(event);
	}
}
