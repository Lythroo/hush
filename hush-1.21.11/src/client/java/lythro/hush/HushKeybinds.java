package lythro.hush;

import com.mojang.blaze3d.platform.InputConstants;
import lythro.hush.Hush;
import lythro.hush.gui.HushConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Hush's key bindings, grouped under their own "Hush" category in Options &gt; Controls.
 *
 * <p>The category label resolves via the lang key {@code key.category.hush.hush}.
 */
public final class HushKeybinds {

	/** Dedicated controls category so Hush's binds don't clutter "Miscellaneous". */
	public static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Hush.MOD_ID, Hush.MOD_ID));

	private HushKeybinds() {
	}

	public static void register() {
		// fabric-api 0.141.x (1.21.11) still exposes the keybind helper under its pre-rename name
		// (keybinding.v1.KeyBindingHelper#registerKeyBinding); the keymapping.v1 module is 26.x-only.
		final KeyMapping openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.hush.open_config",
			InputConstants.UNKNOWN.getValue(), // unbound by default
			CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfig.consumeClick()) {
				// Opened from in-game (no parent screen); the seam supplies the version-correct "show" call.
				HushPlatform.openScreen(new HushConfigScreen(null));
			}
		});
	}
}
