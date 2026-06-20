package lythro.hush;

import lythro.hush.config.HushConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class HushClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Tell the shared core where this loader keeps its config, then load config/hush.json so the
		// sound engine mixin has rules ready.
		HushPlatform.setConfigDir(FabricLoader.getInstance().getConfigDir());
		// 26.2 shows screens via setScreenAndShow (renamed from setScreen in 26.1).
		HushPlatform.setScreenOpener(screen -> Minecraft.getInstance().setScreenAndShow(screen));
		// SoundInstance exposes getIdentifier() on 1.21.11+/26.x (it was getLocation() on 1.21.1).
		HushPlatform.setSoundIdExtractor(instance -> instance.getIdentifier().toString());
		HushConfig.load();
		// Register the "Open Hush Settings" keybind (under its own controls category).
		HushKeybinds.register();
	}
}
