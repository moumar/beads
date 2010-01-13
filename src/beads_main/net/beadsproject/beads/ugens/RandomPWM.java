package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

/**
 * A simple random-length pulse wave modulator. This UGen generates constant
 * pulses of lengths randomly distributed between a minimum length and a maximum
 * length (specified in samples). Additionally, the distribution of the randomly
 * controlled by setting the pulse length exponent parameter (see
 * {@link #setLengthExponent(float) setLengthExponent}).
 * <p>
 * A RandomPWM instance has three modes:
 * <ul>
 * <li>{@link #ALTERNATING} (default) - pulses alternate between -1 and 1.</li>
 * <li>{@link #PULSING} (default) - pulses alternate between 0 and 1.</li>
 * <li>{@link #NOISE} - pulses are distributed continuously between -1 and 1.</li>
 * </ul>
 * 
 * @author Benito Crawford
 * @version 0.9.1
 * 
 */
public class RandomPWM extends UGen {
	public final static int ALTERNATING = 0;
	public final static int NOISE = 1;
	public final static int PULSING = 2;

	protected int mod = 0;
	protected float val = 0;
	protected float minlen = 1, maxlen = 1, len = 0;
	protected float lenexp = 1;
	protected float lenscale;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The audio context.
	 * @param mode
	 *            The pulse mode; see {@link #setMode(int) setMode}.
	 * @param minl
	 *            The minimum pulse length.
	 * @param maxl
	 *            The maximum pulse length.
	 * @param lexp
	 *            The pulse length exponent.
	 */
	public RandomPWM(AudioContext context, int mode, float minl, float maxl,
			float lexp) {
		super(context, 0, 1);
		setParams(mode, minl, maxl, lexp);
	}

	public void calculateBuffer() {
		float[] bo = bufOut[0];

		if (mod == PULSING) {
			for (int i = 0; i < bo.length; i++) {
				if (len <= 0) {
					float d = (float) Math.pow(Math.random(), lenexp)
							* lenscale + minlen;
					len += d;
					if (val > 0)
						val = 0;
					else
						val = 1;
				}
				bo[i] = val;
				len--;
			}
		} else if (mod == ALTERNATING) {
			for (int i = 0; i < bo.length; i++) {
				if (len <= 0) {
					float d = (float) Math.pow(Math.random(), lenexp)
							* lenscale + minlen;
					len += d;
					if (val > 0)
						val = -1;
					else
						val = 1;
				}
				bo[i] = val;
				len--;
			}
		} else {
			for (int i = 0; i < bo.length; i++) {
				if (len <= 0) {
					float d = (float) Math.pow(Math.random(), lenexp)
							* lenscale + minlen;
					len += d;
					val = (float) (Math.random() * 2 - 1);
				}
				bo[i] = val;
				len--;
			}
		}

	}

	/**
	 * Sets the pulse mode (see {@link #setMode(int) setMode}), minimum pulse
	 * length, maximum pulse length, and pulse length exponent.
	 * 
	 * @param mode
	 *            The pulse mode.
	 * @param minl
	 *            The minimum pulse length.
	 * @param maxl
	 *            The maximum pulse length.
	 * @param lexp
	 *            The pulse length exponent.
	 */
	public void setParams(int mode, float minl, float maxl, float lexp) {
		setParams(minl, maxl, lexp);
		setMode(mode);
	}

	/**
	 * Sets the minimum pulse length, maximum pulse length, and pulse length
	 * exponent.
	 * 
	 * @param minl
	 *            The minimum pulse length.
	 * @param maxl
	 *            The maximum pulse length.
	 * @param lexp
	 *            The pulse length exponent.
	 */
	public void setParams(float minl, float maxl, float lexp) {
		setLengthExponent(lexp);
		minlen = Math.max(minl, 1);
		maxlen = Math.max(minlen, maxl);
		lenscale = maxlen - minlen;
	}

	/**
	 * Sets the minimum pulse length.
	 * 
	 * @param minl
	 *            The minimum pulse length.
	 */
	public void setMinLength(float minl) {
		setParams(minl, maxlen, lenexp);
	}

	/**
	 * Gets the minimum pulse length.
	 * 
	 * @return The minimum pulse length.
	 */
	public float getMinLength() {
		return minlen;
	}

	/**
	 * Sets the maximum pulse length.
	 * 
	 * @param maxl
	 *            The maximum pulse length.
	 */
	public void setMaxLength(float maxl) {
		setParams(minlen, maxl, lenexp);
	}

	/**
	 * Gets the maximum pulse length.
	 * 
	 * @return The maximum pulse length.
	 */
	public float getMaxLength() {
		return maxlen;
	}

	/**
	 * Sets the pulse length exponent. This parameter controls the distribution
	 * of pulse lengths: a value of 1 produces a linear distribution; greater
	 * than 1 skews the distribution toward the minimum length; less than one
	 * skews it toward the maximum length.
	 * 
	 * @param lexp
	 *            The pulse length exponent.
	 */
	public void setLengthExponent(float lexp) {
		lenexp = Math.max(lexp, .001f);
	}

	/**
	 * Gets the pulse length exponent.
	 * 
	 * @return The pulse length exponent.
	 * @see #setLengthExponent(float)
	 */
	public float getLengthExponent() {
		return lenexp;
	}

	/**
	 * Sets the pulse mode. Use {@link #ALTERNATING} for pulses that alternate
	 * between -1 and 1; use {@link #PULSING} for pulses that alternate between
	 * 0 and 1; and use {@link #NOISE} for pulses distributed randomly between
	 * -1 and 1.
	 * 
	 * @param mode
	 *            The pulse mode.
	 */
	public void setMode(int mode) {
		mod = mode;
	}

	/**
	 * Gets the pulse mode.
	 * 
	 * @return The pulse mode.
	 * @see #setMode(int)
	 */
	public int getMode() {
		return mod;
	}
}
