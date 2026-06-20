package lythro.hush;

import lythro.hush.config.HushConfig;
import lythro.hush.gui.HushConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client entry point. Only constructed on the physical client (see {@code dist}), so it's safe to
 * touch client-only code here.
 *
 * <p>Wires up the three things Hush needs on the client:
 * <ul>
 *   <li>loads {@code config/hush.json} so the sound-engine mixin has rules ready;</li>
 *   <li>registers the "Open Hush Settings" keybind and its in-game handler
 *       (see {@link HushKeyMappings});</li>
 *   <li>exposes the config screen as this mod's config button in the NeoForge mod list.</li>
 * </ul>
 */
@Mod(value = Hush.MODID, dist = Dist.CLIENT)
public final class HushClient {

	public HushClient(final IEventBus modEventBus, final ModContainer container) {
		// Tell the shared core where this loader keeps its config, then load it before any sound plays.
		HushPlatform.setConfigDir(FMLPaths.CONFIGDIR.get());
		// 26.2 shows screens via setScreenAndShow (renamed from setScreen in 26.1).
		HushPlatform.setScreenOpener(screen -> Minecraft.getInstance().setScreenAndShow(screen));
		// SoundInstance exposes getIdentifier() on 1.21.11+/26.x (it was getLocation() on 1.21.1).
		HushPlatform.setSoundIdExtractor(instance -> instance.getIdentifier().toString());
		HushConfig.load();

		// Keybind registration is a mod-bus event; the per-tick consume check is a game-bus event.
		modEventBus.addListener(HushKeyMappings::onRegisterKeyMappings);
		NeoForge.EVENT_BUS.addListener(HushKeyMappings::onClientTick);

		// "Config" button for Hush in the NeoForge mod list, opening the same screen as the keybind.
		container.registerExtensionPoint(IConfigScreenFactory.class,
			(mc, parent) -> new HushConfigScreen(parent));
	}
}
