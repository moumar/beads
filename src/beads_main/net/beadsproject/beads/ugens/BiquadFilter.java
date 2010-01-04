package net.beadsproject.beads.ugens;

import net.beadsproject.beads.ugens.BiquadCustomCoeffCalculator;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.AudioContext;

/**
 * A simple implementation of a biquad filter. It calculates coefficients based
 * on three parameters (frequency, Q, and gain - the latter only relevant for EQ
 * and shelving filters), each of which may be specified by a static float or by
 * the output of a UGen.
 * 
 * <p>
 * BiquadFilter can be used with pre-programmed algorithms that calculate
 * coefficients for various filter types. (See {@link #setType(int)} for a list
 * of available types.)
 * </p>
 * 
 * <p>
 * BiquadFilter can also implement a user-defined filter algorithm by calling
 * {@link #setCustomType(BiquadCustomCoeffCalculator)}.
 * </p>
 * 
 * @author Benito Crawford
 * @version 0.9.3
 */
public class BiquadFilter extends UGen {

	/**
	 * Indicates a low-pass filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 */
	public final static int LP = 0;

	/**
	 * Indicates a high-pass filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 */
	public final static int HP = 1;

	/**
	 * Indicates a band-pass filter with constant skirt gain; coefficients are
	 * calculated from equations given in "Cookbook formulae for audio EQ biquad
	 * filter coefficients" by Robert Bristow-Johnson.
	 */
	public final static int BP_SKIRT = 2;

	/**
	 * Indicates a band-pass filter with constant peak gain; coefficients are
	 * calculated from equations given in "Cookbook formulae for audio EQ biquad
	 * filter coefficients" by Robert Bristow-Johnson.
	 */
	public final static int BP_PEAK = 3;

	/**
	 * Indicates a notch (band-reject) filter; coefficients are calculated from
	 * equations given in
	 * "Cookbook formulae for audio EQ biquad filter coefficients" by Robert
	 * Bristow-Johnson.
	 */
	public final static int NOTCH = 4;

	/**
	 * Indicates an all-pass filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 */
	public final static int AP = 5;

	/**
	 * Indicates a peaking-EQ filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 * 
	 * <em>untested!</em>
	 */
	public final static int PEAKING_EQ = 6;

	/**
	 * Indicates a low-shelf filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 * 
	 * <em>untested!</em>
	 */
	public final static int LOW_SHELF = 7;

	/**
	 * Indicates a high-shelf filter; coefficients are calculated from equations
	 * given in "Cookbook formulae for audio EQ biquad filter coefficients" by
	 * Robert Bristow-Johnson.
	 * 
	 * <em>untested!</em>
	 */
	public final static int HIGH_SHELF = 8;

	/**
	 * Indicates a user-defined filter; see {@link setCustomType}. This constant
	 * is not recognized by {@link setType}.
	 */
	public final static int CUSTOM_FILTER = 100;

	private float a0 = 1;
	private float a1 = 0;
	private float a2 = 0;
	private float b0 = 0;
	private float b1 = 0;
	private float b2 = 0;

	private float freq = 100, q = 1, gain = 0;
	private int type = -1;
	private float samplingfreq, two_pi_over_sf;

	private float bo1 = 0, bo2 = 0, bi1 = 0, bi2 = 0;
	// private float bi0 = 0;

	private int currsamp;

	private ValCalculator vc;
	private ParamUpdater pu1, pu2;
	private UGen freqUGen, qUGen, gainUGen;

	/**
	 * Constructor for frequency and Q as floats. See {@link setType} for a list
	 * of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link LP}, {@link HP},
	 *            {@link BP_SKIRT}, etc.
	 * @param ifreq
	 *            The initial frequency.
	 * @param iqval
	 *            The initial Q-value.
	 */
	public BiquadFilter(AudioContext context, int itype, float ifreq,
			float iqval) {
		super(context, 1, 1);
		samplingfreq = context.getSampleRate();
		two_pi_over_sf = (float) (Math.PI * 2 / samplingfreq);
		constructPUs();
		setParams(itype, ifreq, iqval);
	}

	/**
	 * Constructor for frequency as a UGen and Q as a float. See {@link setType}
	 * for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, {@link LP}, {@link HP},
	 *            {@link BP_SKIRT}, etc.
	 * @param ifreq
	 *            The frequency UGen.
	 * @param iqval
	 *            The initial Q-value.
	 */
	public BiquadFilter(AudioContext context, int itype, UGen ifreq, float iqval) {
		super(context, 1, 1);
		samplingfreq = context.getSampleRate();
		two_pi_over_sf = (float) (Math.PI * 2 / samplingfreq);

		freqUGen = ifreq;
		q = iqval;
		constructPUs();
		setType(itype);
	}

	/**
	 * Constructor for frequency as a float and Q as a UGen. See {@link setType}
	 * for a list of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link LP}, {@link HP},
	 *            {@link BP_SKIRT}, etc.
	 * @param ifreq
	 *            The initial frequency.
	 * @param iqval
	 *            The Q-value UGen.
	 */
	public BiquadFilter(AudioContext context, int itype, float ifreq, UGen iqval) {
		super(context, 1, 1);
		samplingfreq = context.getSampleRate();
		two_pi_over_sf = (float) (Math.PI * 2 / samplingfreq);

		freq = ifreq;
		qUGen = iqval;
		constructPUs();
		setType(itype);
	}

	/**
	 * Constructor for frequency and Q as UGens. See {@link setType} for a list
	 * of supported filter types.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param itype
	 *            The initial filter type, e.g. {@link LP}, {@link HP},
	 *            {@link BP_SKIRT}, etc.
	 * @param ifreq
	 *            The frequency UGen.
	 * @param iqval
	 *            The Q-value UGen.
	 */
	public BiquadFilter(AudioContext context, int itype, UGen ifreq, UGen iqval) {
		super(context, 1, 1);
		samplingfreq = context.getSampleRate();
		two_pi_over_sf = (float) (Math.PI * 2 / samplingfreq);

		freqUGen = ifreq;
		qUGen = iqval;
		constructPUs();
		setType(itype);
	}

	/**
	 * Sets up a {@link BiquadFilter.ParamUpdater} instance for the filter.
	 */
	private void constructPUs() {
		int ps;
		if (freqUGen != null) {
			ps = 1;
		} else {
			ps = 0;
		}
		if (qUGen != null) {
			ps += 2;
		}

		if (pu1 == null || ps != pu1.type) {
			switch (ps) {
			case 0:
				pu1 = new ParamUpdater(0);
				break;
			case 1:
				pu1 = new ParamUpdater(1) {
					void updateUGens() {
						freqUGen.update();
					}

					void updateParams() {
						freq = freqUGen.getValue(0, currsamp);
						vc.calcVals();
					}
				};
				break;
			case 2:
				pu1 = new ParamUpdater(2) {
					void updateUGens() {
						qUGen.update();
					}

					void updateParams() {
						q = qUGen.getValue(0, currsamp);
						vc.calcVals();
					}
				};
				break;
			case 3:
				pu1 = new ParamUpdater(3) {
					void updateUGens() {
						freqUGen.update();
						qUGen.update();
					}

					void updateParams() {
						freq = freqUGen.getValue(0, currsamp);
						q = qUGen.getValue(0, currsamp);
						vc.calcVals();
					}
				};
				break;
			}
		}

		if (gainUGen != null) {
			if (pu2 == null || pu2.type != 1) {
				pu2 = new ParamUpdater(1) {
					void updateUGens() {
						gainUGen.update();
					}

					void updateParams() {
						gain = gainUGen.getValue(0, currsamp);
					}
				};
			}
		} else {
			if (pu2 == null || pu2.type != 0) {
				pu2 = new ParamUpdater(0);
			}
		}

	}

	@Override
	public void calculateBuffer() {
		float[] bi = bufIn[0];
		float[] bo = bufOut[0];

		pu1.updateUGens();
		pu2.updateUGens();

		// first two samples
		currsamp = 0;
		pu1.updateParams();
		pu2.updateParams();
		bo[0] = (b0 * bi[0] + b1 * bi1 + b2 * bi2 - a1 * bo1 - a2 * bo2) / a0;

		// old method: see below for explanation
		// bi0 = bi[0];
		// float y = ((b0 * bi0) + (b1 * bi1) + (b2 * bi2) - (a1 * bo1) - (a2 *
		// bo2)) / a0;
		// bo[0] = y;
		// bo2 = bo1;
		// bo1 = y;
		// bi2 = bi1;
		// bi1 = bi0;

		currsamp = 1;
		pu1.updateParams();
		pu2.updateParams();
		bo[1] = (b0 * bi[1] + b1 * bi[0] + b2 * bi1 - a1 * bo[0] - a2 * bo1)
				/ a0;
		// old method: see below for explanation
		// bi0 = bi[1];
		// y = ((b0 * bi0) + (b1 * bi1) + (b2 * bi2) - (a1 * bo1) - (a2 * bo2))
		// / a0;
		// bo[1] = y;
		// bo2 = bo1;
		// bo1 = y;
		// bi2 = bi1;
		// bi1 = bi0;

		// main loop
		for (currsamp = 2; currsamp < bufferSize; currsamp++) {
			pu1.updateParams();
			pu2.updateParams();

			// "access the arrays a lot" method
			// i originally thought that this would be slower due to
			// array-access time,
			// but it seems that it's a touch faster than maintaining running
			// variables
			// (somewhere between 80-97% depending, according to my naive
			// tests).
			bo[currsamp] = (b0 * bi[currsamp] + b1 * bi[currsamp - 1] + b2
					* bi[currsamp - 2] - a1 * bo[currsamp - 1] - a2
					* bo[currsamp - 2])
					/ a0;

			// "don't access them a lot" method

			// bi0 = bi[currsamp];
			// y = ((b0 * bi0) + (b1 * bi1) + (b2 * bi2) - (a1 * bo1) - (a2 *
			// bo2)) / a0;
			// bo[currsamp] = y;
			// bo2 = bo1;
			// bo1 = y;
			// bi2 = bi1;
			// bi1 = bi0;
		}

		// these are needed for "access the arrays" method - they provide 2
		// samples of
		// "memory" between sample vectors
		bi1 = bi[bufferSize - 1];
		bi2 = bi[bufferSize - 2];
		bo1 = bo[bufferSize - 1];
		bo2 = bo[bufferSize - 2];

		// check to make sure filter didn't blow up
		if (Float.isNaN(bi1) || Float.isNaN(bo1))
			reset();

	}

	/**
	 * Resets the filter in case it "explodes".
	 */
	public void reset() {
		bi1 = 0;
		bi2 = 0;
		bo1 = 0;
		bo2 = 0;
	}

	private class ValCalculator {
		void calcVals() {
		};
	}

	private class LPValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			float cosw = (float) Math.cos(w);
			float a = (float) Math.sin(w) / q * .5f;
			b1 = 1 - cosw;
			b2 = b0 = b1 * .5f;
			a0 = 1 + a;
			a1 = -2 * cosw;
			a2 = 1 - a;
		}
	}

	private class HPValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			float cosw = (float) Math.cos(w);
			float a = (float) Math.sin(w) / q * .5f;
			b1 = -1 - cosw;
			b2 = b0 = b1 * -.5f;
			a0 = 1 + a;
			a1 = -2 * cosw;
			a2 = 1 - a;
		}
	}

	private class BPSkirtValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			float sinw = (float) Math.sin(w);
			float a = sinw / q * .5f;
			b1 = 0;
			b2 = 0 - (b0 = sinw * .5f);
			a0 = 1 + a;
			a1 = -2 * (float) Math.cos(w);
			a2 = 1 - a;
		}
	}

	private class BPPeakValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			// float a = (float) Math.sin(w) / q * .5f;
			b1 = 0;
			b2 = 0 - (b0 = (float) Math.sin(w) / q * .5f);
			a0 = 1 + b0;
			a1 = -2 * (float) Math.cos(w);
			a2 = 1 - b0;
		}
	}

	private class NotchValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			float a = (float) Math.sin(w) / q * .5f;
			b2 = b0 = 1;
			a1 = b1 = -2 * (float) Math.cos(w);
			a0 = 1 + a;
			a2 = 1 - a;
		}
	}

	private class APValCalculator extends ValCalculator {
		void calcVals() {
			float w = two_pi_over_sf * freq;
			float a = (float) (Math.sin(w) / q * .5);
			a2 = b0 = 1 - a;
			a1 = b1 = (float) (-2 * Math.cos(w));
			a0 = b2 = 1 + a;
		}
	}

	private class PeakingEQValCalculator extends ValCalculator {
		void calcVals() {
			float A = (float) Math.pow(10, gain * .025);
			float w = two_pi_over_sf * freq;
			// float cosw = (float) Math.cos(w);
			float a = (float) (Math.sin(w) / q * .5);
			b2 = 2 - (b0 = 1 + a * A);
			a1 = b1 = -2 * (float) Math.cos(w);
			a2 = 2 - (a0 = 1 + a / A);
			/*
			 * peakingEQ: H(s) = (s^2 + s*(A/Q) + 1) / (s^2 + s/(A*Q) + 1)
			 * 
			 * b0 = 1 + alpha*A b1 = -2*cos(w0) b2 = 1 - alpha*A a0 = 1 +
			 * alpha/A a1 = -2*cos(w0) a2 = 1 - alpha/A
			 */
		}
	}

	private class LowShelfValCalculator extends ValCalculator {
		void calcVals() {
			float A = (float) Math.pow(10, gain * .025);
			float w = two_pi_over_sf * freq;
			float cosw = (float) Math.cos(w);
			float a = (float) (Math.sin(w) / q * .5);
			float b = 2 * a * (float) Math.sqrt(A);
			float c = (A - 1) * cosw;
			b0 = A * (A + 1 - c + b);
			b1 = 2 * A * ((A - 1) - (A + 1) * cosw);
			b2 = A * (A + 1 - c - b);
			a0 = A + 1 + c + b;
			a1 = -2 * ((A - 1) + (A + 1) * cosw);
			a2 = A + 1 + c - b;
			/*
			 * lowShelf: H(s) = A * (s^2 + (sqrt(A)/Q)*s + A)/(A*s^2 +
			 * (sqrt(A)/Q)*s + 1)
			 * 
			 * b0 = A*( (A+1) - (A-1)*cos(w0) + 2*sqrt(A)*alpha ) b1 = 2*A*(
			 * (A-1) - (A+1)*cos(w0) ) b2 = A*( (A+1) - (A-1)*cos(w0) -
			 * 2*sqrt(A)*alpha ) a0 = (A+1) + (A-1)*cos(w0) + 2*sqrt(A)*alpha a1
			 * = -2*( (A-1) + (A+1)*cos(w0) ) a2 = (A+1) + (A-1)*cos(w0) -
			 * 2*sqrt(A)*alpha
			 */
		}
	}

	private class HighShelfValCalculator extends ValCalculator {
		void calcVals() {
			float A = (float) Math.pow(10, gain * .025);
			float w = two_pi_over_sf * freq;
			float cosw = (float) Math.cos(w);
			float a = (float) (Math.sin(w) / q * .5);
			float b = 2 * a * (float) Math.sqrt(A);
			float c = (A - 1) * cosw;

			b0 = A * (A + 1 - c + b);
			b1 = -2 * A * ((A - 1) - (A + 1) * cosw);
			b2 = A * (A + 1 - c - b);
			a0 = A + 1 + c + b;
			a1 = 2 * (A - 1 + (A + 1) * cosw);
			a2 = A + 1 + c - b;
			/*
			 * highShelf: H(s) = A * (A*s^2 + (sqrt(A)/Q)*s + 1)/(s^2 +
			 * (sqrt(A)/Q)*s + A)
			 * 
			 * b0 = A*( (A+1) + (A-1)*cos(w0) + 2*sqrt(A)*alpha ) b1 =
			 * -2*A*((A-1) + (A+1)*cos(w0) ) b2 = A*( (A+1) + (A-1)*cos(w0) -
			 * 2*sqrt(A)*alpha ) a0 = (A+1) - (A-1)*cos(w0) + 2*sqrt(A)*alpha a1
			 * = 2*( (A-1) - (A+1)*cos(w0) ) a2 = (A+1) - (A-1)*cos(w0) -
			 * 2*sqrt(A)*alpha
			 */
		}
	}

	/**
	 * The coeffiecent calculator that interfaces with a
	 * {@link BiquadCustomCoefficientCalculator} to allow user-defined filter
	 * algorithms.
	 * 
	 * @author benito
	 * @version .9
	 */
	private class CustomValCalculator extends ValCalculator {
		BiquadCustomCoeffCalculator ccc;

		CustomValCalculator(BiquadCustomCoeffCalculator iccc) {
			ccc = iccc;
		}

		void calcVals() {
			ccc.calcCoeffs(freq, q, gain);
			a0 = ccc.a0;
			a1 = ccc.a1;
			a2 = ccc.a2;
			b0 = ccc.b0;
			b1 = ccc.b1;
			b2 = ccc.b2;
		}
	}

	/**
	 * The class archetype that allows for more efficient updating of filter
	 * parameters when some parameters are fixed.
	 */
	private class ParamUpdater {
		int type;

		ParamUpdater(int type) {
			this.type = type;
		}

		void updateUGens() {
		}

		void updateParams() {
		}
	}

	/**
	 * Sets the parameters for the filter, including the type. (For a list of
	 * supported types, see {@link setType}).
	 * 
	 * @param ntype
	 *            The filter type, e.g. {@link #LP}, {@link #HP},
	 *            {@link #BP_SKIRT} , etc.
	 * @param nfreq
	 *            The filter frequency.
	 * @param nqval
	 *            The filter Q-value.
	 */
	public void setParams(int ntype, float nfreq, float nqval) {
		freq = nfreq;
		q = nqval;
		setType(ntype);
	}

	/**
	 * Sets the parameters for the filter.
	 * 
	 * @param nfreq
	 *            The filter frequency.
	 * @param nqval
	 *            The filter Q-value.
	 */
	public void setParams(float nfreq, float nqval) {
		freq = nfreq;
		q = nqval;
		vc.calcVals();
	}

	/**
	 * Sets the type of filter. To set a custom type, use {@link setCustomType}.
	 * The following types are recognized:
	 * <ul>
	 * <li>{@link #LP} - Low-pass filter.</li>
	 * <li>{@link #HP} - High-pass filter.</li>
	 * <li>{@link #BP_SKIRT} - Band-pass filter with constant skirt gain.</li>
	 * <li>{@link #BP_PEAK} - Band-pass filter with constant peak gain.</li>
	 * <li>{@link #NOTCH} - Notch (band-reject) filter.</li>
	 * <li>{@link #AP} - All-pass filter.</li>
	 * <li>{@link #PEAKING_EQ} - Peaking-EQ filter.</li>
	 * <li>{@link #LOW_SHELF} - Low-shelf filter.</li>
	 * <li>{@link #HIGH_SHELF} - High-shelf filter.</li>
	 * </ul>
	 * 
	 * @param ntype
	 *            The type of filter.
	 */
	public void setType(int ntype) {
		if (ntype != type || vc == null) {
			int t = type;
			type = ntype;
			switch (type) {
			case LP:
				vc = new LPValCalculator();
				break;
			case HP:
				vc = new HPValCalculator();
				break;
			case BP_SKIRT:
				vc = new BPSkirtValCalculator();
				break;
			case BP_PEAK:
				vc = new BPPeakValCalculator();
				break;
			case NOTCH:
				vc = new NotchValCalculator();
				break;
			case AP:
				vc = new APValCalculator();
				break;
			case PEAKING_EQ:
				vc = new PeakingEQValCalculator();
				break;
			case LOW_SHELF:
				vc = new LowShelfValCalculator();
				break;
			case HIGH_SHELF:
				vc = new HighShelfValCalculator();
				break;
			default:
				type = t;
				break;
			}
			vc.calcVals();
		}
	}

	/**
	 * Gets the type of the filter.
	 * 
	 * @return The filter type.
	 * @see setType
	 */
	public int getType() {
		return type;
	}

	/**
	 * Gets the current filter frequency. If the frequency has just been set to
	 * a UGen, it may not have been updated.
	 * 
	 * @return The filter frequency.
	 */
	public float getFreq() {
		return freq;
	}

	/**
	 * Sets the filter frequency to a float value. This will remove the
	 * frequency UGen, if there is one.
	 * 
	 * @param nfreq
	 *            The frequency.
	 */
	public void setFreq(float nfreq) {
		freq = nfreq;
		freqUGen = null;
		constructPUs();
		vc.calcVals();
	}

	/**
	 * Sets a UGen to determine the filter frequency.
	 * 
	 * @param nfreq
	 *            The frequency UGen.
	 */
	public void setFreq(UGen nfreq) {
		freqUGen = nfreq;
		constructPUs();
	}

	/**
	 * Gets the frequency UGen, if there is one.
	 * 
	 * @return The frequency UGen.
	 */
	public UGen getFreqUGen() {
		return freqUGen;
	}

	/**
	 * Sets the filter Q-value to a float. This will remove the Q UGen if there
	 * is one.
	 * 
	 * @param nqval
	 *            The Q-value.
	 */
	public void setQ(float nqval) {
		q = nqval;
		qUGen = null;
		constructPUs();
		vc.calcVals();
	}

	/**
	 * Sets a UGen to determine the filter Q-value.
	 * 
	 * @param nqval
	 *            The Q-value UGen.
	 */
	public void setQ(UGen nqval) {
		qUGen = nqval;
		constructPUs();
	}

	/**
	 * Gets the current Q-value for the filter. If the Q-value has just been set
	 * to a UGen, it may not have been updated.
	 * 
	 * @return The current Q-value.
	 */
	public float getQ() {
		return q;
	}

	/**
	 * Gets the Q UGen, if there is one.
	 * 
	 * @return The Q UGen.
	 */
	public UGen getUGen() {
		return qUGen;
	}

	/**
	 * Sets the filter gain to a float. This will remove the gain UGen if there
	 * is one. (Only relevant for {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and
	 * {@link #HIGH_SHELF} types.)
	 * 
	 * @param ngain
	 *            The gain in decibels (0 means no gain).
	 */
	public void setGain(float ngain) {
		gain = ngain;
		gainUGen = null;
		constructPUs();
		vc.calcVals();
	}

	/**
	 * Sets a UGen to determine the filter Q-value. (Only relevant for
	 * {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and {@link #HIGH_SHELF} types.)
	 * 
	 * @param ngain
	 *            The gain UGen, specifying the gain in decibels.
	 */
	public void setGain(UGen ngain) {
		gainUGen = ngain;
		constructPUs();
	}

	/**
	 * Gets the current gain in decibels for the filter. If the gain has just
	 * been set to a UGen, it may not have been updated. (Only relevant for
	 * {@link #PEAKING_EQ}, {@link #LOW_SHELF}, and {@link #HIGH_SHELF} types.)
	 * 
	 * @return The current gain.
	 */
	public float getGain() {
		return gain;
	}

	/**
	 * Gets the gain UGen, if there is one.
	 * 
	 * @return The gain UGen.
	 */
	public UGen getGainUGen() {
		return gainUGen;
	}

	/**
	 * Sets a user-defined coefficient calculation algorithm. The algorithm is
	 * defined in a user-defined class that extends
	 * {@link BiquadCustomCoeffCalculator}.
	 * 
	 * @param cc
	 *            The custom coefficient calculator.
	 */

	public void setCustomType(BiquadCustomCoeffCalculator cc) {
		vc = new CustomValCalculator(cc);
		vc.calcVals();
	}

}
