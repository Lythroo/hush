package lythro.hush;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod constants. Hush is a client-side mod (see {@code "environment": "client"} in fabric.mod.json and
 * the {@code client} entrypoint {@link HushClient}); there is no common/server initialiser.
 */
public final class Hush {

	public static final String MOD_ID = "hush";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private Hush() {
	}
}
