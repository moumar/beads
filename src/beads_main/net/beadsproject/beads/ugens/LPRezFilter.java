package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

/**
 * A simple 2nd-order resonant low-pass filter. Faster than a BiquadFilter
 * because its algorithm is of the form:
 * <p>
 * y(n) = b0 * x(n) + a1 * y(n-1) + a2 * y(n-2);
 * <p>
 * so it doesn't compute unnecessary parts of the biquad formula.
 * <p>
 * Takes two parameters: cut-off frequency and resonance (0 for no resonance, 1
 * for maximum resonance).
 * 
 * @author Benito Crawford
 * @version 0.9.2
 */
public class LPRezFilter extends UGen {

	protected float freq = 100;
	protected float res = 0;
	protected UGen freqUGen, resUGen;

	private float cosw = 0, _2pi_over_sr, a1, a2, b0;
	private float y1 = 0, y2 = 0;
	private int currsamp;
	protected ParamUpdater pu;

	/**
	 * Constructor for frequency and resonance specified by floats.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The filter cut-off frequency.
	 * @param res
	 *            The resonance.
	 */
	public LPRezFilter(AudioContext con, float freq, float res) {
		super(con, 1, 1);
		_2pi_over_sr = (float) (2 * Math.PI / con.getSampleRate());
		setFreq(freq);
		setRes(res);
	}

	/**
	 * Constructor for frequency specified by a UGen and resonance specified by
	 * a float.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The filter cut-off frequency UGen.
	 * @param res
	 *            The resonance.
	 */
	public LPRezFilter(AudioContext con, UGen freq, float res) {
		super(con, 1, 1);
		_2pi_over_sr = (float) (2 * Math.PI / con.getSampleRate());
		setFreq(freq);
		setRes(res);
	}

	/**
	 * Constructor for frequency specified by a float and resonance specified by
	 * a UGen.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The filter cut-off frequency.
	 * @param res
	 *            The resonance UGen.
	 */
	public LPRezFilter(AudioContext con, float freq, UGen res) {
		super(con, 1, 1);
		_2pi_over_sr = (float) (2 * Math.PI / con.getSampleRate());
		setFreq(freq);
		setRes(res);
	}

	/**
	 * Constructor for frequency and resonance specified by UGens.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The filter cut-off frequency UGen.
	 * @param res
	 *            The resonance UGen.
	 */
	public LPRezFilter(AudioContext con, UGen freq, UGen res) {
		super(con, 1, 1);
		_2pi_over_sr = (float) (2 * Math.PI / con.getSampleRate());
		setFreq(freq);
		setRes(res);
	}

	protected void constructPU() {
		int c = 0;
		if (freqUGen != null) {
			c += 1;
		}
		if (resUGen != null) {
			c += 2;
		}

		if (pu == null || pu.type != c) {
			switch (c) {
			case 0:
				pu = new ParamUpdater(0);
				break;
			case 1:
				pu = new ParamUpdater(1) {
					void updateUGens() {
						freqUGen.update();
					}

					void updateParams() {
						freq = freqUGen.getValue(0, currsamp);
						cosw = (float) (Math.cos(_2pi_over_sr * freq));
						calcVals();
					}
				};
				break;
			case 2:
				pu = new ParamUpdater(2) {
					void updateUGens() {
						resUGen.update();
					}

					void updateParams() {
						float r = resUGen.getValue(0, currsamp);
						if (r > .99999) {
							res = .99999f;
						} else if (r < 0) {
							res = 0;
						} else {
							res = r;
						}
						calcVals();
					}
				};
				break;
			case 3:
				pu = new ParamUpdater(3) {
					void updateUGens() {
						freqUGen.update();
						resUGen.update();
					}

					void updateParams() {
						freq = freqUGen.getValue(0, currsamp);
						cosw = (float) (Math.cos(_2pi_over_sr * freq));
						float r = resUGen.getValue(0, currsamp);
						if (r > .999999f) {
							res = .999999f;
						} else if (r < 0) {
							res = 0;
						} else {
							res = r;
						}
						calcVals();
					}
				};
				break;
			}

		}
	}

	protected void calcVals() {
		a1 = -2 * res * cosw;
		a2 = res * res;
		b0 = 1 + a1 + a2;
	}

	@Override
	public void calculateBuffer() {

		float[] bi = bufIn[0];
		float[] bo = bufOut[0];

		pu.updateUGens();

		currsamp = 0;
		pu.updateParams();
		bo[0] = bi[0] * b0 - a1 * y1 - a2 * y2;
		currsamp = 1;
		bo[1] = bi[1] * b0 - a1 * bo[0] - a2 * y1;

		// main loop
		for (currsamp = 2; currsamp < bufferSize; currsamp++) {
			bo[currsamp] = bi[currsamp] * b0 - a1 * bo[currsamp - 1] - a2
					* bo[currsamp - 2];
		}

		y1 = bo[bufferSize - 1];
		y2 = bo[bufferSize - 2];
		if (Float.isNaN(y1)) {
			reset();
		}
	}

	/**
	 * Resets the filter in case it "explodes".
	 */
	public void reset() {
		y1 = 0;
		y2 = 0;
	}

	/**
	 * For efficiency.
	 */
	protected class ParamUpdater {
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
	 * Gets the current cut-off frequency.
	 * 
	 * @return The cut-off frequency.
	 */
	public float getFreq() {
		return freq;
	}

	/**
	 * Sets the cut-off frequency to a float. Removes the frequency UGen, if
	 * there is one.
	 * 
	 * @param f
	 *            The cut-off frequency.
	 */
	public void setFreq(float f) {
		freq = f;
		cosw = (float) (Math.cos(_2pi_over_sr * freq));
		freqUGen = null;
		constructPU();
		calcVals();
	}

	/**
	 * Sets a UGen to specify the cut-off frequency.
	 * 
	 * @param f
	 *            The frequency UGen.
	 */
	public void setFreq(UGen f) {
		freqUGen = f;
		constructPU();
	}

	/**
	 * Gets the frequency UGen, if it exists.
	 * 
	 * @return The frequency UGen.
	 */
	public UGen getFreqUGen() {
		return freqUGen;
	}

	/**
	 * Gets the current resonance value.
	 * 
	 * @return The resonance.
	 */
	public float getRes() {
		return res;
	}

	/**
	 * Sets the filter resonance to a float value. This removes the resonance
	 * UGen, if it exists. (Should be >= 0 and < 1.)
	 * 
	 * @param r
	 *            The resonance.
	 */
	public void setRes(float r) {
		if (r > .999999f) {
			res = .999999f;
		} else if (r < 0) {
			res = 0;
		} else {
			res = r;
		}
		resUGen = null;
		constructPU();
		calcVals();
	}

	/**
	 * Sets a UGen to specify the filter resonance.
	 * 
	 * @param r
	 *            The resonance UGen.
	 */
	public void setRes(UGen r) {
		resUGen = r;
		constructPU();
	}

	/**
	 * Gets the resonance UGen, if it exists.
	 * 
	 * @return The resonance UGen.
	 */
	public UGen getResUGen() {
		return resUGen;
	}

}
