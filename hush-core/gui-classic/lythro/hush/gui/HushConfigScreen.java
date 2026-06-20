package lythro.hush.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/**
 * Immediate-mode 1.21.1 entry point for the shared config screen.
 *
 * <p>Rendering hooks the same {@code render(GuiGraphics, …)} as the retained-mode 1.21.x line
 * (see {@code gui-legacy}), but 1.21.1 predates the input refactor: its mouse callbacks still use the
 * classic {@code (double mouseX, double mouseY, int button)} signatures instead of a
 * {@code MouseButtonEvent}. Those native overrides live here and forward to the shared primitive-typed
 * {@code hushMouse*} handlers in {@link HushConfigScreenBase}.
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
	public boolean mouseClicked(final double mx, final double my, final int button) {
		return super.mouseClicked(mx, my, button) || hushMouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(final double mx, final double my, final int button, final double dragX, final double dragY) {
		return hushMouseDragged(mx) || super.mouseDragged(mx, my, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(final double mx, final double my, final int button) {
		return hushMouseReleased() || super.mouseReleased(mx, my, button);
	}
}
