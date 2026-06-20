package lythro.hush;

import com.mojang.blaze3d.platform.InputConstants;
import lythro.hush.gui.HushConfigScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Hush's key bindings, grouped under their own "Hush" category in Options &gt; Controls.
 *
 * <p>On 1.21.1 a key category is just a translation-key string ({@code key.category.hush.hush}); the
 * {@code KeyMapping.Category} type only arrives in later versions. Registration is a mod-bus event
 * ({@link RegisterKeyMappingsEvent}); the per-tick "was it pressed?" check is a game-bus event
 * ({@link ClientTickEvent.Post}). Both listeners are wired up from {@link HushClient}.
 */
public final class HushKeyMappings {

	/** Dedicated controls category (a lang key on 1.21.1) so Hush's binds don't clutter "Miscellaneous". */
	public static final String CATEGORY = "key.category.hush.hush";

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
