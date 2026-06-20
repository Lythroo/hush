package lythro.hush;

import com.mojang.blaze3d.platform.InputConstants;
import lythro.hush.gui.HushConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/**
 * Hush's key bindings, grouped under their own "Hush" category in Options &gt; Controls.
 *
 * <p>The category label resolves via the lang key {@code key.category.hush.hush}.
 */
public final class HushKeybinds {

	/** Dedicated controls category (a lang key on 1.21.1) so Hush's binds don't clutter "Miscellaneous". */
	public static final String CATEGORY = "key.category.hush.hush";

	private HushKeybinds() {
	}

	public static void register() {
		// 1.21.1 predates KeyMapping.Category, so the category is just a translation-key String. fabric-api
		// exposes the keybind helper under its pre-rename name (keybinding.v1.KeyBindingHelper).
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
