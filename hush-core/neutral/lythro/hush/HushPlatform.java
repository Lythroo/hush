package lythro.hush;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;

/**
 * Tiny seam for the handful of things that differ between mod loaders <em>and</em> between Minecraft
 * versions. Each target's client init fills these in once at startup, before any shared code needs
 * them, so the cross-loader / cross-version core never has to import loader-specific types
 * ({@code FabricLoader}, {@code FMLPaths}) or branch on a version-specific API.
 *
 * <p>Example of the latter: opening a screen moved from {@code Minecraft.setScreen} (≤26.1) to
 * {@code Minecraft.setScreenAndShow} / {@code Gui.setScreen} (26.2+). Rather than fork the shared
 * config screen, each target supplies a {@link #setScreenOpener opener} that knows its own call.
 */
public final class HushPlatform {

	private static Path configDir;
	private static Consumer<Screen> screenOpener;
	private static Function<SoundInstance, String> soundId;

	private HushPlatform() {
	}

	/** Set by each loader's client initializer (Fabric: {@code FabricLoader.getConfigDir()}; NeoForge: {@code FMLPaths.CONFIGDIR}). */
	public static void setConfigDir(final Path dir) {
		configDir = dir;
	}

	/** The platform config directory. Throws if a loader forgot to set it before use. */
	public static Path configDir() {
		if (configDir == null) {
			throw new IllegalStateException("HushPlatform.configDir not set — call HushPlatform.setConfigDir() in client init");
		}
		return configDir;
	}

	/** Supplies the version-correct "show this screen" call (handles the 26.1→26.2 API move). */
	public static void setScreenOpener(final Consumer<Screen> opener) {
		screenOpener = opener;
	}

	/** Shows {@code screen} (or returns to the game when {@code null}) via the registered opener. */
	public static void openScreen(final @org.jspecify.annotations.Nullable Screen screen) {
		if (screenOpener == null) {
			throw new IllegalStateException("HushPlatform.screenOpener not set — call HushPlatform.setScreenOpener() in client init");
		}
		screenOpener.accept(screen);
	}

	/**
	 * Supplies the version-correct way to read a sound's resource id. {@code SoundInstance}'s accessor
	 * was renamed ({@code getLocation()} on 1.21.1 → {@code getIdentifier()} on 1.21.11+/26.x) and its
	 * return type renamed alongside ({@code ResourceLocation} → {@code Identifier}), so the shared
	 * sound mixin can name neither — it asks here for the id already reduced to a {@code String}.
	 */
	public static void setSoundIdExtractor(final Function<SoundInstance, String> extractor) {
		soundId = extractor;
	}

	/** The resource id of {@code instance} (e.g. {@code "minecraft:entity.zombie.ambient"}). */
	public static String soundId(final SoundInstance instance) {
		if (soundId == null) {
			throw new IllegalStateException("HushPlatform.soundId not set — call HushPlatform.setSoundIdExtractor() in client init");
		}
		return soundId.apply(instance);
	}
}
