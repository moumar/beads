package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.*;

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
 * <li>{@link #SAW} - for random-length ramps between -1 and 1.</li>
 * <li>{@link #RAMPED_NOISE} - for random-length ramps between random values.</li>
 * </ul>
 * 
 * @author Benito Crawford
 * @version 0.9.1
 * 
 */
public class RandomPWM extends UGen implements DataBeadReceiver {
	public final static int ALTERNATING = 0;
	public final static int NOISE = 1;
	public final static int PULSING = 2;
	public final static int SAW = 3;
	public final static int RAMPED_NOISE = 4;

	protected int mode = 0;
	protected float targetVal = 0, baseVal = 0, valDiff = 0;
	protected float count = 0, pulseLen = 0;
	protected float minLength = 10, maxLength = 100, lengthExponent = 1;
	protected float lengthDiff = 0;

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

		if (mode == PULSING) {
			for (int i = 0; i < bo.length; i++) {
				if (count <= 0) {
					calcVals();
					if (targetVal > 0) {
						targetVal = 0;
					} else {
						targetVal = 1;
					}
					valDiff = targetVal - baseVal;
				}
				bo[i] = targetVal;
				count--;
			}
		} else if (mode == ALTERNATING) {
			for (int i = 0; i < bo.length; i++) {
				if (count <= 0) {
					calcVals();
					if (targetVal > 0) {
						targetVal = -1;
					} else {
						targetVal = 1;
					}
					valDiff = targetVal - baseVal;
				}
				bo[i] = targetVal;
				count--;
			}
		} else if (mode == SAW) {
			for (int i = 0; i < bo.length; i++) {
				if (count <= 0) {
					calcVals();
					if (targetVal > 0) {
						targetVal = -1;
					} else {
						targetVal = 1;
					}
					valDiff = targetVal - baseVal;
				}
				bo[i] = targetVal - ((float) count / pulseLen) * valDiff;
				count--;
			}
		} else if (mode == RAMPED_NOISE) {
			for (int i = 0; i < bo.length; i++) {
				if (count <= 0) {
					calcVals();
					targetVal = (float) (Math.random() * 2 - 1);
					valDiff = targetVal - baseVal;
				}
				bo[i] = targetVal - ((float) count / pulseLen) * valDiff;
				count--;
			}
		} else {
			// for NOISE
			for (int i = 0; i < bo.length; i++) {
				if (count <= 0) {
					calcVals();
					targetVal = (float) (Math.random() * 2 - 1);
					valDiff = targetVal - baseVal;
				}
				bo[i] = targetVal;
				count--;
			}
		}

	}

	protected void calcVals() {
		float d = (float) Math.pow(Math.random(), lengthExponent) * lengthDiff
				+ minLength;
		count += d;
		pulseLen = count;
		baseVal = targetVal;
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
	public RandomPWM setParams(int mode, float minl, float maxl, float lexp) {
		setParams(minl, maxl, lexp);
		setMode(mode);
		return this;
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
	public RandomPWM setParams(float minl, float maxl, float lexp) {
		setLengthExponent(lexp);
		minLength = Math.max(minl, 1);
		maxLength = Math.max(minLength, maxl);
		lengthDiff = maxLength - minLength;
		return this;
	}

	/**
	 * Sets the minimum pulse length.
	 * 
	 * @param minl
	 *            The minimum pulse length.
	 */
	public RandomPWM setMinLength(float minl) {
		setParams(minl, maxLength, lengthExponent);
		return this;
	}

	/**
	 * Gets the minimum pulse length.
	 * 
	 * @return The minimum pulse length.
	 */
	public float getMinLength() {
		return minLength;
	}

	/**
	 * Sets the maximum pulse length.
	 * 
	 * @param maxl
	 *            The maximum pulse length.
	 */
	public RandomPWM setMaxLength(float maxl) {
		setParams(minLength, maxl, lengthExponent);
		return this;
	}

	/**
	 * Gets the maximum pulse length.
	 * 
	 * @return The maximum pulse length.
	 */
	public float getMaxLength() {
		return maxLength;
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
	public RandomPWM setLengthExponent(float lexp) {
		if ((lengthExponent = lexp) < .001f) {
			lengthExponent = .001f;
		}
		return this;
	}

	/**
	 * Gets the pulse length exponent.
	 * 
	 * @return The pulse length exponent.
	 * @see #setLengthExponent(float)
	 */
	public float getLengthExponent() {
		return lengthExponent;
	}

	/**
	 * Sets the pulse mode.
	 * <p>
	 * <ul>
	 * <li>Use {@link #ALTERNATING} for pulses that alternate between -1 and 1.</li>
	 * <li>Use {@link #PULSING} for pulses that alternate between 0 and 1.</li>
	 * <li>Use {@link #NOISE} for pulses distributed randomly between -1 and 1.</li>
	 * <li>Use {@link #SAW} for random-length ramps between -1 and 1.</li>
	 * <li>Use {@link #RAMPED_NOISE} for random-length ramps between random
	 * values.</li>
	 * </ul>
	 * 
	 * @param mode
	 *            The pulse mode.
	 */
	public RandomPWM setMode(int mode) {
		this.mode = mode;
		return this;
	}

	/**
	 * Gets the pulse mode.
	 * 
	 * @return The pulse mode.
	 * @see #setMode(int)
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Use the properties "mode", "minLength", "maxLength", and "lengthExponent"
	 * to set the corresponding parameters (floats only).
	 */
	public DataBeadReceiver sendData(DataBead db) {
		setParams((int) db.getFloat("mode", mode), db.getFloat("minLength",
				minLength), db.getFloat("maxLength", maxLength), db.getFloat(
				"lengthExponent", lengthExponent));
		return this;
	}

	public void messageReceived(Bead message) {
		if(message instanceof DataBead) {
			sendData((DataBead) message);
		}
	}
	
	/**
	 * Gets a DataBead filled with properties corresponding to this object's
	 * parameters.
	 * 
	 * @return The parameter DataBead.
	 */
	public DataBead getParams() {
		DataBead db = new DataBead();
		db.put("mode", mode);
		db.put("minLength", minLength);
		db.put("maxLength", maxLength);
		db.put("lengthExponent", lengthExponent);
		return db;
	}
}
