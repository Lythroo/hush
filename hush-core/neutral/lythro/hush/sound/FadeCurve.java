package lythro.hush.sound;

/**
 * Shape of a fade-out over time. Given linear progress {@code p} (0 at fade start, 1 at the end),
 * {@link #remaining(float)} returns the volume factor (1 → 0).
 *
 * <p>{@link #SMOOTH} (a cosine ease-in-out) is the default: it eases away from full volume and into
 * silence, which sounds far less abrupt than a straight linear cut.
 */
public enum FadeCurve {

	/** Constant-rate fade. */
	LINEAR("Linear") {
		@Override
		public float remaining(final float p) {
			return 1.0f - p;
		}
	},

	/** Cosine ease-in-out: gentle at both ends, smoothest perceived fade. */
	SMOOTH("Smooth") {
		@Override
		public float remaining(final float p) {
			return 0.5f * (1.0f + (float) Math.cos(Math.PI * p));
		}
	},

	/** Quadratic ease-out: drops away then trails into a soft tail. */
	EXPONENTIAL("Exponential") {
		@Override
		public float remaining(final float p) {
			final float r = 1.0f - p;
			return r * r;
		}
	};

	private final String label;

	FadeCurve(final String label) {
		this.label = label;
	}

	public String label() {
		return this.label;
	}

	/** Volume factor (1 → 0) at linear progress {@code p} in [0, 1]. */
	public abstract float remaining(float p);

	/** The next curve, for cycling through options in the UI. */
	public FadeCurve next() {
		final FadeCurve[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}
