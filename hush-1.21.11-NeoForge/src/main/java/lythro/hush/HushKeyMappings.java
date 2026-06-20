package lythro.hush;

import com.mojang.blaze3d.platform.InputConstants;
import lythro.hush.gui.HushConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Hush's key bindings, grouped under their own "Hush" category in Options &gt; Controls.
 *
 * <p>The category label resolves via the lang key {@code key.category.hush.hush}. Registration is a
 * mod-bus event ({@link RegisterKeyMappingsEvent}); the per-tick "was it pressed?" check is a
 * game-bus event ({@link ClientTickEvent.Post}). Both listeners are wired up from {@link HushClient}.
 */
public final class HushKeyMappings {

	/** Dedicated controls category so Hush's binds don't clutter "Miscellaneous". */
	public static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Hush.MODID, Hush.MODID));

	/** Opens the config screen; unbound by default so it never clashes with another mod's key. */
	public static final KeyMapping OPEN_CONFIG = new KeyMapping(
		"key.hush.open_config",
		InputConstants.UNKNOWN.getValue(),
		CATEGORY
	);

	private HushKeyMappings() {
	}

	public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
		event.register(OPEN_CONFIG);
	}

	public static void onClientTick(final ClientTickEvent.Post event) {
		while (OPEN_CONFIG.consumeClick()) {
			// Opened from in-game (no parent screen); the seam supplies the version-correct "show" call.
			HushPlatform.openScreen(new HushConfigScreen(null));
		}
	}
}
