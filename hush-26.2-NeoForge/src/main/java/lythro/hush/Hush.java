package lythro.hush;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Common mod entry point.
 *
 * <p>Hush is functionally a client-side mod (it reshapes how sounds are heard), so almost everything
 * lives in {@link HushClient}. This class exists only to anchor the mod id and shared logger; it does
 * nothing on a dedicated server.
 */
@Mod(Hush.MODID)
public final class Hush {

	public static final String MODID = "hush";
	public static final Logger LOGGER = LogUtils.getLogger();

	public Hush(final IEventBus modEventBus, final ModContainer container) {
		// No common-side registration: the sound engine mixin is client-gated and the config is loaded
		// from HushClient. Kept intentionally empty so the mod loads harmlessly on servers too.
	}
}
