package lythro.hush;

import com.mojang.blaze3d.platform.InputConstants;
import lythro.hush.Hush;
import lythro.hush.gui.HushConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
		final KeyMapping openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
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
