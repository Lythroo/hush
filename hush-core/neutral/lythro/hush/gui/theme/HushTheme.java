package lythro.hush.gui.theme;

/**
 * Central palette and metrics for Hush's UI.
 *
 * <p>Every colour and key dimension lives here so the look stays consistent and can be retuned in
 * one place. Colours are {@code 0xAARRGGBB}. The panel is opaque and a full-screen scrim is drawn
 * behind it, so colours render true regardless of what's in the world behind the screen.
 */
public final class HushTheme {

	// --- backdrop & panel ---
	public static final int SCRIM = 0xB0000000;
	public static final int PANEL = 0xFF1E2129;
	public static final int PANEL_BORDER = 0xFF565E73;
	public static final int SEARCH_BG = 0xFF15181F;
	public static final int SEARCH_BORDER = 0xFF4A5266;
	public static final int DIVIDER = 0xFF3A4051;

	// --- text ---
	public static final int TITLE = 0xFFFFFFFF;
	public static final int TEXT = 0xFFE9EBF1;
	public static final int TEXT_MUTED = 0xFFAAB1C2;
	public static final int TEXT_DISABLED = 0xFF6E7689;
	public static final int SECTION = 0xFF7C859B;

	// --- rows ---
	public static final int ROW = 0xFF272B36;
	public static final int ROW_HOVER = 0xFF323845;
	public static final int ROW_BORDER = 0xFF474F62;

	// --- accent (bright: toggle-on pill, tab underline, title accent) ---
	public static final int ACCENT = 0xFF36C6B3;
	public static final int KNOB = 0xFFEDEFF4;
	public static final int TRACK = 0xFF454B5B;

	// --- tabs ---
	public static final int TAB_TEXT = 0xFFB9C0D0;
	public static final int TAB_ACTIVE_TEXT = 0xFFFFFFFF;
	public static final int TAB_ACTIVE_BG = 0xFF2C313D;

	// --- button bases (gradient + stroke + text are derived from these) ---
	private static final int BTN = 0xFF333845;
	private static final int BTN_HOVER = 0xFF3E4452;
	private static final int BTN_TEXT = 0xFFDDE1EA;
	private static final int ACCENT_BTN = 0xFF1C9A8B;
	private static final int ACCENT_BTN_HOVER = 0xFF25B0A0;
	private static final int ACCENT_BTN_TEXT = 0xFFEAFBF7; // light mint — deliberately not black

	/** Visual style for a {@link lythro.hush.gui.widget.Widgets#button} press target. */
	public record ButtonStyle(int bg, int bgHover, int border, int text) {
	}

	public static final ButtonStyle NEUTRAL_BUTTON =
		new ButtonStyle(BTN, BTN_HOVER, Colors.lighten(BTN, 0.28f), BTN_TEXT);
	public static final ButtonStyle ACCENT_BUTTON =
		new ButtonStyle(ACCENT_BTN, ACCENT_BTN_HOVER, Colors.lighten(ACCENT_BTN, 0.30f), ACCENT_BTN_TEXT);

	private HushTheme() {
	}
}
