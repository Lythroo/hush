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
		// 1.21.x shows screens via the classic Minecraft.setScreen (renamed to setScreenAndShow in 26.2).
		HushPlatform.setScreenOpener(screen -> Minecraft.getInstance().setScreen(screen));
		// 1.21.1's SoundInstance still exposes getLocation() (renamed to getIdentifier() in 1.21.11+).
		HushPlatform.setSoundIdExtractor(instance -> instance.getLocation().toString());
		HushConfig.load();
		// Register the "Open Hush Settings" keybind (under its own controls category).
		HushKeybinds.register();
	}
}
