/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.DataBead;

/**
 * A simple multi-channel 2nd-order resonant low-pass filter. Faster than a
 * BiquadFilter because its algorithm is of the form:
 * <p>
 * <code>y(n) = b0 * x(n) + a1 * y(n-1) + a2 * y(n-2)</code>
 * <p>
 * so it doesn't compute unnecessary parts of the biquad formula.
 * <p>
 * Takes two parameters: cut-off frequency and resonance (0 for no resonance, 1
 * for maximum resonance). These parameters can be set using
 * {@link #setFreq(float) setFreq()} and {@link #setRes(float) setRes()}, or by
 * passing a DataBead with "frequency" and "resonance" properties to
 * {@link #setParams(DataBead)}. (Messaging this object with a DataBead achieves
 * the same.)
 * 
 * @beads.category filter
 * @author Benito Crawford
 * @version 0.9.5
 */
public class LPRezFilterMulti extends LPRezFilter {

	private float[] y1m, y2m;
	private int channels;

	/**
	 * Default constructor for an n-channel filter.
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 */
	public LPRezFilterMulti(AudioContext con, int channels) {
		super(con, channels);
		this.channels = super.getOuts();
		y1m = new float[this.channels];
		y2m = new float[this.channels];
	}

	/**
	 * Constructor for frequency and resonance specified by floats.
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 * @param freq
	 *            The filter cut-off frequency.
	 * @param res
	 *            The resonance.
	 */
	public LPRezFilterMulti(AudioContext con, int channels, float freq,
			float res) {
		this(con, channels);
		setFreq(freq).setRes(res);
	}

	/**
	 * Constructor for frequency specified by a UGen and resonance specified by
	 * a float.
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 * @param freq
	 *            The filter cut-off frequency UGen.
	 * @param res
	 *            The resonance.
	 */
	public LPRezFilterMulti(AudioContext con, int channels, UGen freq, float res) {
		this(con, channels);
		setFreq(freq).setRes(res);
	}

	/**
	 * Constructor for frequency specified by a float and resonance specified by
	 * a UGen.
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 * @param freq
	 *            The filter cut-off frequency.
	 * @param res
	 *            The resonance UGen.
	 */
	public LPRezFilterMulti(AudioContext con, int channels, float freq, UGen res) {
		this(con, channels);
		setFreq(freq).setRes(res);
	}

	/**
	 * Constructor for frequency and resonance specified by UGens.
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 * @param freq
	 *            The filter cut-off frequency UGen.
	 * @param res
	 *            The resonance UGen.
	 */
	public LPRezFilterMulti(AudioContext con, int channels, UGen freq, UGen res) {
		this(con, channels);
		setFreq(freq).setRes(res);
	}

	@Override
	public void calculateBuffer() {

		if (isFreqStatic && isResStatic) {
			for (int i = 0; i < channels; i++) {
				float[] bi = bufIn[i];
				float[] bo = bufOut[i];

				bo[0] = bi[0] * b0 - a1 * y1m[i] - a2 * y2m[i];
				bo[1] = bi[1] * b0 - a1 * bo[0] - a2 * y1m[i];

				// main loop
				for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
					bo[currsamp] = bi[currsamp] * b0 - a1 * bo[currsamp - 1]
							- a2 * bo[currsamp - 2];
				}

				y2m[i] = bo[bufferSize - 2];
				if (Float.isNaN(y1m[i] = bo[bufferSize - 1])) {
					reset();
				}
			}

		} else {

			freqUGen.update();
			resUGen.update();

			// first sample
			cosw = (float) (Math.cos(_2pi_over_sr
					* (freq = freqUGen.getValue(0, 0))));
			if ((res = resUGen.getValue(0, 0)) > .999999f) {
				res = .999999f;
			} else if (res < 0) {
				res = 0;
			}
			calcVals();
			for (int i = 0; i < channels; i++) {
				bufOut[i][0] = bufIn[i][0] * b0 - a1 * y1m[i] - a2 * y2m[i];
			}

			// second sample
			cosw = (float) (Math.cos(_2pi_over_sr
					* (freq = freqUGen.getValue(0, 1))));
			if ((res = resUGen.getValue(0, 1)) > .999999f) {
				res = .999999f;
			} else if (res < 0) {
				res = 0;
			}
			calcVals();
			for (int i = 0; i < channels; i++) {
				bufOut[i][1] = bufIn[i][1] * b0 - a1 * bufOut[i][0] - a2
						* y1m[i];
			}

			// main loop
			for (int currsamp = 2; currsamp < bufferSize; currsamp++) {

				cosw = (float) (Math.cos(_2pi_over_sr
						* (freq = freqUGen.getValue(0, currsamp))));
				if ((res = resUGen.getValue(0, currsamp)) > .999999f) {
					res = .999999f;
				} else if (res < 0) {
					res = 0;
				}
				calcVals();

				for (int i = 0; i < channels; i++) {
					bufOut[i][currsamp] = bufIn[i][currsamp] * b0 - a1
							* bufOut[i][currsamp - 1] - a2
							* bufOut[i][currsamp - 2];
				}
			}

			for (int i = 0; i < channels; i++) {
				y2m[i] = bufOut[i][bufferSize - 2];
				if (Float.isNaN(y1m[i] = bufOut[i][bufferSize - 1])) {
					reset();
				}
			}
		}

	}

	/**
	 * Resets the filter in case it "explodes".
	 */
	public void reset() {
		for (int i = 0; i < channels; i++) {
			y1m[i] = 0;
			y2m[i] = 0;
		}
	}

	public LPRezFilterMulti setFreq(float f) {
		super.setFreq(f);
		return this;
	}

	public LPRezFilterMulti setFreq(UGen f) {
		super.setFreq(f);
		return this;
	}

	public LPRezFilterMulti setRes(float r) {
		super.setRes(r);
		return this;
	}

	public LPRezFilterMulti setRes(UGen r) {
		super.setRes(r);
		return this;
	}

	public LPRezFilterMulti setParams(DataBead paramBead) {
		super.setParams(paramBead);
		return this;
	}

}
