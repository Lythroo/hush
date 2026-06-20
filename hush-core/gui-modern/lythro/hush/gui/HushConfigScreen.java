package lythro.hush.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/**
 * 26.x entry point for the shared config screen.
 *
 * <p>Minecraft 26.x draws screens via {@code extractRenderState(GuiGraphicsExtractor, …)}. We wrap
 * that graphics object in a {@link Gfx} adapter and delegate to {@link HushConfigScreenBase#draw},
 * passing a runnable that invokes the native widget render (the search box) at the correct point
 * inside the panel's pose transform. All actual layout/drawing lives in the shared base.
 *
 * <p>The retained-mode mouse callbacks take a {@code MouseButtonEvent}; we unpack it and forward to
 * the shared primitive-typed {@code hushMouse*} handlers (immediate-mode 1.21.1 forwards differently).
 */
public final class HushConfigScreen extends HushConfigScreenBase {

	public HushConfigScreen(final @Nullable Screen parent) {
		super(parent);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float a) {
		draw(new Gfx(g), mouseX, mouseY, () -> super.extractRenderState(g, mouseX, mouseY, a));
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
