package lythro.hush.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Subtle UI feedback sounds for the config screen. All play through the UI sound channel at the
 * vanilla UI volume, with pitch varied per action so interactions feel distinct.
 *
 * <p>These play {@code ui.button.click} etc., which Hush doesn't manage, so they pass through the
 * sound engine mixin untouched.
 */
public final class HushSounds {

	private HushSounds() {
	}

	private static void play(final Holder<SoundEvent> sound, final float pitch) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
	}

	private static void play(final SoundEvent sound, final float pitch) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
	}

	/** Generic click (Done, tab). */
	public static void click() {
		play(SoundEvents.UI_BUTTON_CLICK, 1.0f);
	}

	/** Switching category tabs. */
	public static void tab() {
		play(SoundEvents.UI_BUTTON_CLICK, 1.08f);
	}

	/** Flipping a toggle: higher pitch when turning on, lower when off. */
	public static void toggle(final boolean on) {
		play(SoundEvents.UI_BUTTON_CLICK, on ? 1.18f : 0.86f);
	}

	/** Nudging volume: pitch rises with the resulting level for tactile feedback. */
	public static void step(final float volume) {
		play(SoundEvents.UI_BUTTON_CLICK, 0.9f + 0.6f * volume);
	}

	/** Opening the screen. */
	public static void open() {
		play(SoundEvents.UI_TOAST_IN, 1.3f);
	}
}
