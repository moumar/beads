/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.DataBead;
import net.beadsproject.beads.ugens.BiquadFilterMulti;

/**
 * A simple implementation of a biquad filter, optimized for single-channel
 * performance. It calculates coefficients based on three parameters (frequency,
 * Q, and gain - the latter only relevant for EQ and shelving filters), each of
 * which may be specified by a static float or by the output of a UGen.
 * <p>
 * Filter parameters may be set with individual setter functions (
 * {@link #setFreq(float) setFreq}, {@link #setQ(float) setQ}, and
 * {@link #setGain(float) setGain}), or by passing a DataBead with the
 * appropriate properties to {@link #setParams(DataBead) setParams}. (Messaging
 * the filter with a DataBead is equivalent to calling setParams.) Setter
 * methods return the instance, so they may be strung together:
 * <p>
 * <code>filt.setFreq(200).setQ(30).setGain(.4);</code>
 * <p>
 * BiquadFilterMulti can be used with pre-programmed algorithms that calculate
 * coefficients for various filter types. (See {@link #setType(int)} for a list
 * of available types.)
 * <p>
 * BiquadFilterMulti can also implement a user-defined filter algorithm by
 * calling {@link #setCustomType(CustomCoeffCalculator)}.
 * 
 * @beads.category filter
 * @author Benito Crawford
 * @version 0.9.5
 */
public class BiquadFilter extends BiquadFilterMulti {

	private float bo1 = 0, bo2 = 0, bi1 = 0, bi2 = 0;

	/**
	 * Constructor for a type of BiquadFilter. See {@link #setType(int) setType}
	 * for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 */
	public BiquadFilter(AudioContext context, int itype) {
		super(context, 1, itype);
	}

	/**
	 * Constructor for a specified type of biquad filter with parameters
	 * specified by a DataBead. See {@link #setType(int) setType} for a list of
	 * supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 * @param params
	 *            A DataBead specifying filter parameters; see
	 *            {@link #setParams(DataBead)}.
	 */
	public BiquadFilter(AudioContext context, int itype, DataBead params) {
		super(context, 1, itype, params);
	}

	/**
	 * Constructor for frequency and Q as floats. See {@link #setType(int)
	 * setType} for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 * @param ifreq
	 *            The initial frequency.
	 * @param iqval
	 *            The initial Q-value.
	 */
	public BiquadFilter(AudioContext context, int itype, float ifreq,
			float iqval) {
		super(context, 1, itype);
		setFreq(ifreq).setQ(iqval);
	}

	/**
	 * Constructor for frequency as a UGen and Q as a float. See
	 * {@link #setType(int) setType} for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 * @param ifreq
	 *            The frequency UGen.
	 * @param iqval
	 *            The initial Q-value.
	 */
	public BiquadFilter(AudioContext context, int itype, UGen ifreq,
			float iqval) {
		super(context, 1, itype);
		setFreq(ifreq).setQ(iqval);
	}

	/**
	 * Constructor for frequency as a float and Q as a UGen. See
	 * {@link #setType(int) setType} for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 * @param ifreq
	 *            The initial frequency.
	 * @param iqval
	 *            The Q-value UGen.
	 */
	public BiquadFilter(AudioContext context, int itype, float ifreq,
			UGen iqval) {
		super(context, 1, itype);
		setFreq(ifreq).setQ(iqval);
	}

	/**
	 * Constructor for frequency and Q as UGens. See {@link #setType(int)
	 * setType} for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT}, etc.
	 * @param ifreq
	 *            The frequency UGen.
	 * @param iqval
	 *            The Q-value UGen.
	 */
	public BiquadFilter(AudioContext context, int itype, UGen ifreq,
			UGen iqval) {
		super(context, 1, itype);
		setFreq(ifreq).setQ(iqval);
	}

	@Override
	public void calculateBuffer() {

		float[] bi = bufIn[0];
		float[] bo = bufOut[0];

		if (areAllStatic) {

			// first two samples
			bo[0] = (b0 * bi[0] + b1 * bi1 + b2 * bi2 - a1 * bo1 - a2 * bo2)
					/ a0;
			bo[1] = (b0 * bi[1] + b1 * bi[0] + b2 * bi1 - a1 * bo[0] - a2 * bo1)
					/ a0;

			// main loop
			for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
				bo[currsamp] = (b0 * bi[currsamp] + b1 * bi[currsamp - 1] + b2
						* bi[currsamp - 2] - a1 * bo[currsamp - 1] - a2
						* bo[currsamp - 2])
						/ a0;
			}

		} else {

			freqUGen.update();
			qUGen.update();
			gainUGen.update();

			// first two samples
			freq = freqUGen.getValue(0, 0);
			q = qUGen.getValue(0, 0);
			gain = gainUGen.getValue(0, 0);
			vc.calcVals();
			bo[0] = (b0 * bi[0] + b1 * bi1 + b2 * bi2 - a1 * bo1 - a2 * bo2)
					/ a0;

			freq = freqUGen.getValue(0, 1);
			q = qUGen.getValue(0, 1);
			gain = gainUGen.getValue(0, 1);
			vc.calcVals();
			bo[1] = (b0 * bi[1] + b1 * bi[0] + b2 * bi1 - a1 * bo[0] - a2 * bo1)
					/ a0;

			// main loop
			for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
				freq = freqUGen.getValue(0, currsamp);
				q = qUGen.getValue(0, currsamp);
				gain = gainUGen.getValue(0, currsamp);
				vc.calcVals();

				bo[currsamp] = (b0 * bi[currsamp] + b1 * bi[currsamp - 1] + b2
						* bi[currsamp - 2] - a1 * bo[currsamp - 1] - a2
						* bo[currsamp - 2])
						/ a0;
			}

		}

		// get 2 samples of "memory" between sample vectors
		bi1 = bi[bufferSize - 1];
		bi2 = bi[bufferSize - 2];
		bo1 = bo[bufferSize - 1];
		bo2 = bo[bufferSize - 2];

		// check to make sure filter didn't blow up
		if (Float.isNaN(bo1))
			reset();

	}

	public void reset() {
		bi1 = 0;
		bi2 = 0;
		bo1 = 0;
		bo2 = 0;
	}

	public BiquadFilter setType(int ntype) {
		super.setType(ntype);
		return this;
	}

	public BiquadFilter setFreq(float nfreq) {
		super.setFreq(nfreq);
		return this;
	}

	public BiquadFilter setFreq(UGen nfreq) {
		super.setFreq(nfreq);
		return this;
	}

	public BiquadFilter setQ(float nqval) {
		super.setQ(nqval);
		return this;
	}

	public BiquadFilter setQ(UGen nqval) {
		super.setQ(nqval);
		return this;
	}

	public BiquadFilter setGain(float ngain) {
		super.setGain(ngain);
		return this;
	}

	public BiquadFilter setGain(UGen ngain) {
		super.setGain(ngain);
		return this;
	}

	public BiquadFilter setCustomType(CustomCoeffCalculator cc) {
		super.setCustomType(cc);
		return this;
	}

}
