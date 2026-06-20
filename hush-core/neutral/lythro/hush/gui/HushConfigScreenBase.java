package lythro.hush.gui;

import java.util.Locale;
import lythro.hush.HushPlatform;
import lythro.hush.config.HushConfig;
import lythro.hush.gui.anim.Anim;
import lythro.hush.gui.anim.AnimTracker;
import lythro.hush.gui.theme.HushTheme;
import lythro.hush.gui.widget.Widgets;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Hush's configuration screen.
 *
 * <p>A single settings page with three sections: <b>General</b> (master switch + the blanket
 * "silence dying entities" toggle), <b>Sounds</b> (named per-sound toggles &mdash; de-spatialise the
 * enderman, tame the portal entry sound) and <b>Fade-out</b> (duration + curve). Drawing and
 * hit-testing are delegated to the reusable {@link Widgets} and {@link HushTheme}.
 */
public abstract class HushConfigScreenBase extends Screen {

	private static final int PAD = 12;
	private static final int ROW_STRIDE = 20;
	private static final int CARD_H = 18;
	private static final int SEC_H = 16;
	/** Vertical space from the panel top to the start of the settings form (title + accent line). */
	private static final int HEADER_H = 30;
	private static final int DONE_GAP = 8;
	private static final int DONE_H = 18;
	private static final int FADE_MAX_MS = 1000;
	private static final int FADE_STEP_MS = 50;

	private final @Nullable Screen parent;
	private final AnimTracker anim = new AnimTracker();

	private boolean draggingFade;

	// animation state
	private long lastNanos;
	private float openProgress;
	private boolean opened;
	private float dt;

	// geometry, computed in init()
	private Rect panel = new Rect(0, 0, 0, 0);
	private Rect content = new Rect(0, 0, 0, 0);
	private Rect done = new Rect(0, 0, 0, 0);

	protected HushConfigScreenBase(final @Nullable Screen parent) {
		super(Component.literal("Hush"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		// Fixed settings form — no tabs, search or scrolling list. The panel is sized to exactly fit the
		// three sections (General, Sounds, Fade-out: each a header + two rows) plus header/Done chrome.
		final int formH = 3 * SEC_H + 6 * ROW_STRIDE;
		final int panelW = Math.min(340, this.width - 60);
		final int panelH = HEADER_H + formH + DONE_GAP + DONE_H + PAD;
		final int panelX = (this.width - panelW) / 2;
		final int panelY = (this.height - panelH) / 2;
		this.panel = new Rect(panelX, panelY, panelW, panelH);

		final int inner = panelW - 2 * PAD;
		this.content = new Rect(panelX + PAD, panelY + HEADER_H, inner, formH);
		this.done = new Rect(this.panel.centerX() - 46, this.content.bottom() + DONE_GAP, 92, DONE_H);

		if (!this.opened) {
			this.opened = true;
			HushSounds.open();
		}
	}

	// --- rendering ---

	/**
	 * Draws the whole screen through the version-neutral {@link G} adapter. The per-version subclass
	 * overrides Minecraft's native render method, wraps its graphics object in a {@code Gfx} adapter,
	 * and passes a {@code nativeWidgets} runnable that invokes {@code super}'s render inside the panel's
	 * pose transform. The screen no longer adds any native widgets, but the runnable is kept so the
	 * per-era subclasses stay unchanged.
	 */
	protected final void draw(final G g, final int mouseX, final int mouseY, final Runnable nativeWidgets) {
		final long now = System.nanoTime();
		this.dt = this.lastNanos == 0 ? 0.0f : Math.min(0.1f, (now - this.lastNanos) / 1_000_000_000.0f);
		this.lastNanos = now;
		this.openProgress = Anim.approach(this.openProgress, 1.0f, this.dt, 12.0f);
		final float op = Anim.easeOutCubic(this.openProgress);

		g.fill(0, 0, this.width, this.height, HushTheme.SCRIM);

		g.pushPose();
		final float scale = 0.97f + 0.03f * op;
		g.translate(this.panel.centerX(), this.panel.centerY());
		g.scale(scale);
		g.translate(-this.panel.centerX(), -this.panel.centerY());
		g.translate(0.0f, (1.0f - op) * 8.0f);

		Widgets.surface(g, this.panel, HushTheme.PANEL, HushTheme.PANEL_BORDER);
		renderHeader(g);
		renderSettings(g, mouseX, mouseY);

		final float doneHover = this.anim.toward("done", this.done.contains(mouseX, mouseY), this.dt);
		Widgets.button(g, this.font, this.done, "Done", HushTheme.ACCENT_BUTTON, doneHover);

		nativeWidgets.run(); // no native widgets remain; kept so per-version entry points are unchanged
		g.popPose();
	}

	private void renderHeader(final G g) {
		final int cx = this.panel.centerX();
		final int cy = this.panel.y() + 12;
		g.pushPose();
		g.translate(cx, cy);
		g.scale(1.4f);
		Widgets.textCentered(g, this.font, "Hush", new Rect(-50, -4, 100, 8), HushTheme.TITLE);
		g.popPose();
		g.fill(cx - 20, this.panel.y() + 20, cx + 20, this.panel.y() + 22, HushTheme.ACCENT);
	}

	/** A section divider: small caps label with a trailing hairline. */
	private void sectionHeader(final G g, final Rect rect, final String label) {
		final String caps = label.toUpperCase(Locale.ROOT);
		Widgets.textLeft(g, this.font, caps, rect.x() + 2, rect, HushTheme.SECTION);
		final int lineX = rect.x() + 2 + this.font.width(caps) + 8;
		g.fill(lineX, rect.centerY(), rect.right() - 2, rect.centerY() + 1, HushTheme.DIVIDER);
	}

	// --- settings ---

	private void renderSettings(final G g, final int mouseX, final int mouseY) {
		final HushConfig cfg = HushConfig.get();
		final SettingsLayout s = settingsLayout();

		sectionHeader(g, s.generalHeader(), "General");
		settingRow(g, s.masterRow(), "Hush enabled");
		Widgets.toggle(g, s.master(), this.anim.toward("set:enabled", cfg.enabled, this.dt));
		settingRow(g, s.silenceRow(), "Silence dying entities");
		Widgets.toggle(g, s.silence(), this.anim.toward("set:silence", cfg.silenceDyingEntities, this.dt));

		sectionHeader(g, s.soundsHeader(), "Sounds");
		settingRow(g, s.endermanRow(), "De-spatialise enderman");
		Widgets.toggle(g, s.enderman(), this.anim.toward("set:enderman", cfg.endermanStereo(), this.dt));
		settingRow(g, s.portalRow(), "Tame portal sound");
		Widgets.toggle(g, s.portal(), this.anim.toward("set:portal", cfg.portalEnabled(), this.dt));

		sectionHeader(g, s.fadeHeader(), "Fade-out");
		settingRow(g, s.fadeRow(), "Duration");
		final float sliderHover = this.anim.toward("set:slider", s.slider().contains(mouseX, mouseY) || this.draggingFade, this.dt);
		Widgets.slider(g, s.slider(), cfg.fade.durationMs / (float) FADE_MAX_MS, sliderHover);
		Widgets.textCentered(g, this.font, cfg.fade.durationMs + " ms", s.value(), HushTheme.TEXT_MUTED);
		settingRow(g, s.curveRow(), "Curve");
		final float curveHover = this.anim.toward("set:curve", s.curve().contains(mouseX, mouseY), this.dt);
		Widgets.button(g, this.font, s.curve(), cfg.fade.curve.label(), HushTheme.NEUTRAL_BUTTON, curveHover);
	}

	private void settingRow(final G g, final Rect card, final String label) {
		Widgets.surface(g, card, HushTheme.ROW, HushTheme.ROW_BORDER);
		Widgets.textLeft(g, this.font, label, card.x() + 10, card, HushTheme.TEXT);
	}

	private SettingsLayout settingsLayout() {
		final int x = this.content.x();
		final int w = this.content.w();
		int y = this.content.y();
		final Rect generalHeader = new Rect(x, y, w, SEC_H);
		y += SEC_H;
		final Rect masterRow = new Rect(x, y, w, CARD_H);
		y += ROW_STRIDE;
		final Rect silenceRow = new Rect(x, y, w, CARD_H);
		y += ROW_STRIDE;
		final Rect soundsHeader = new Rect(x, y, w, SEC_H);
		y += SEC_H;
		final Rect endermanRow = new Rect(x, y, w, CARD_H);
		y += ROW_STRIDE;
		final Rect portalRow = new Rect(x, y, w, CARD_H);
		y += ROW_STRIDE;
		final Rect fadeHeader = new Rect(x, y, w, SEC_H);
		y += SEC_H;
		final Rect fadeRow = new Rect(x, y, w, CARD_H);
		y += ROW_STRIDE;
		final Rect curveRow = new Rect(x, y, w, CARD_H);

		final int h = 14;
		final Rect master = toggleRect(masterRow, h);
		final Rect silence = toggleRect(silenceRow, h);
		final Rect enderman = toggleRect(endermanRow, h);
		final Rect portal = toggleRect(portalRow, h);
		final Rect value = new Rect(fadeRow.right() - 8 - 58, fadeRow.y(), 58, fadeRow.h());
		final int sliderLeft = fadeRow.x() + 90;
		final Rect slider = new Rect(sliderLeft, fadeRow.centerY() - h / 2, value.x() - 10 - sliderLeft, h);
		final Rect curve = new Rect(curveRow.right() - 8 - 110, curveRow.centerY() - 8, 110, 16);
		return new SettingsLayout(generalHeader, masterRow, silenceRow, soundsHeader, endermanRow, portalRow,
			fadeHeader, fadeRow, curveRow, master, silence, enderman, portal, slider, value, curve);
	}

	/** A 28×{@code h} toggle hit-box anchored to the right edge of {@code row}. */
	private static Rect toggleRect(final Rect row, final int h) {
		return new Rect(row.right() - 8 - 28, row.centerY() - h / 2, 28, h);
	}

	private boolean handleSettingsClick(final double mx, final double my) {
		final HushConfig cfg = HushConfig.get();
		final SettingsLayout s = settingsLayout();
		if (s.master().contains(mx, my)) {
			cfg.enabled = !cfg.enabled;
			HushSounds.toggle(cfg.enabled);
			return true;
		}
		if (s.silence().contains(mx, my)) {
			cfg.silenceDyingEntities = !cfg.silenceDyingEntities;
			HushSounds.toggle(cfg.silenceDyingEntities);
			return true;
		}
		if (s.enderman().contains(mx, my)) {
			cfg.setEndermanStereo(!cfg.endermanStereo());
			HushSounds.toggle(cfg.endermanStereo());
			return true;
		}
		if (s.portal().contains(mx, my)) {
			cfg.setPortalEnabled(!cfg.portalEnabled());
			HushSounds.toggle(cfg.portalEnabled());
			return true;
		}
		if (s.curve().contains(mx, my)) {
			cfg.fade.curve = cfg.fade.curve.next();
			HushSounds.click();
			return true;
		}
		if (s.slider().contains(mx, my)) {
			this.draggingFade = true;
			setFadeFromMouse(mx, s.slider());
			HushSounds.click();
			return true;
		}
		return false;
	}

	private void setFadeFromMouse(final double mx, final Rect slider) {
		final float p = Math.max(0.0f, Math.min(1.0f, (float) ((mx - slider.x()) / slider.w())));
		HushConfig.get().fade.durationMs = Math.round(p * FADE_MAX_MS / FADE_STEP_MS) * FADE_STEP_MS;
	}

	private record SettingsLayout(Rect generalHeader, Rect masterRow, Rect silenceRow, Rect soundsHeader,
								  Rect endermanRow, Rect portalRow, Rect fadeHeader, Rect fadeRow, Rect curveRow,
								  Rect master, Rect silence, Rect enderman, Rect portal, Rect slider, Rect value, Rect curve) {
	}

	// --- input ---
	//
	// The native mouse signatures diverge: retained-mode 1.21.8+/26.x take a MouseButtonEvent,
	// immediate-mode 1.21.1 takes (double mx, double my, int button). So the actual Screen overrides
	// live in each per-era HushConfigScreen subclass, which unpacks the coordinates and delegates to
	// these primitive-typed helpers.

	/** Done-button / settings hit-testing for a primary-button click at {@code (mx, my)}. */
	protected boolean hushMouseClicked(final double mx, final double my, final int button) {
		if (button != 0) {
			return false;
		}
		if (this.done.contains(mx, my)) {
			HushSounds.click();
			onClose();
			return true;
		}
		return handleSettingsClick(mx, my);
	}

	/** Drags the fade slider when one is in progress; {@code mx} is the cursor x. */
	protected boolean hushMouseDragged(final double mx) {
		if (this.draggingFade) {
			setFadeFromMouse(mx, settingsLayout().slider());
			return true;
		}
		return false;
	}

	/** Ends a fade-slider drag, if one is in progress. */
	protected boolean hushMouseReleased() {
		if (this.draggingFade) {
			this.draggingFade = false;
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		HushConfig.save();
		// Screen management differs by MC version (Minecraft.setScreen vs Gui.setScreen); the seam
		// supplies the right call so this shared screen stays version-neutral.
		HushPlatform.openScreen(this.parent);
	}
}
