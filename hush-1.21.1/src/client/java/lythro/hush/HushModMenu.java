package lythro.hush;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import lythro.hush.gui.HushConfigScreen;

/**
 * Adds a config button for Hush in Mod Menu's mod list. This class is only loaded when Mod Menu is
 * installed (it's registered via the {@code modmenu} entrypoint), so Mod Menu stays an optional
 * dependency &mdash; the keybind opens the same screen without it.
 */
public class HushModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return HushConfigScreen::new; // parent -> new HushConfigScreen(parent)
	}
}
