package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

/**
 * A simple one-pole filter implementation. Cut-off frequency can be specified
 * either by UGen or a float.
 * <p>
 * It uses the formula: y(n) = a * x(n) + (1 - a) * y(n - 1)
 * 
 * @author Benito Crawford
 * @version 0.9
 */
public class OnePoleFilter extends UGen {
	private float freq, a0, b1, y1 = 0;
	private int currsamp;
	private UGen freqUGen;
	protected float samplingfreq, two_pi_over_sf;
	protected ParamUpdater pu;

	/**
	 * Constructor for cut-off frequency specified by a static float.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The cut-off frequency.
	 */
	public OnePoleFilter(AudioContext con, float freq) {
		super(con, 1, 1);
		samplingfreq = con.getSampleRate();
		two_pi_over_sf = (float) (2 * Math.PI / samplingfreq);

		setFreq(freq);
	}

	/**
	 * Constructor for cut-off frequency specified by a UGen.
	 * 
	 * @param con
	 *            The audio context.
	 * @param freq
	 *            The cut-off frequency UGen.
	 */
	public OnePoleFilter(AudioContext con, UGen freq) {
		super(con, 1, 1);
		samplingfreq = con.getSampleRate();
		two_pi_over_sf = (float) (2 * Math.PI / samplingfreq);

		setFreq(freq);
	}

	protected void constructPU() {
		if (freqUGen == null) {
			if (pu == null || pu.type != 0) {
				pu = new ParamUpdater(0);
			}
		} else {
			if (pu == null || pu.type != 1) {
				pu = new ParamUpdater(0) {
					void updateUGens() {
						freqUGen.update();
					}

					void updateParams() {
						freq = freqUGen.getValue(0, currsamp);
						calcVals();
					}
				};
			}
		}

		calcVals();
	}

	protected void calcVals() {
		a0 = (float) Math.sin(two_pi_over_sf * freq);
		b1 = a0 - 1;
	}

	@Override
	public void calculateBuffer() {
		pu.updateUGens();

		float[] bi = bufIn[0];
		float[] bo = bufOut[0];

		for (currsamp = 0; currsamp < bufferSize; currsamp++) {
			pu.updateParams();
			bo[currsamp] = y1 = a0 * bi[currsamp] - b1 * y1;
		}

		// check to see if it blew up
		if (Float.isNaN(y1))
			y1 = 0;
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
	 * Sets the cut-off frequency to a static float.
	 * 
	 * @param freq
	 *            The cut-off frequency.
	 */
	public void setFreq(float freq) {
		this.freq = freq;
		constructPU();
	}

	/**
	 * Sets a UGen to specify the cut-off frequency.
	 * 
	 * @param freqUGen
	 *            The cut-off frequency UGen.
	 */
	public void setFreq(UGen freqUGen) {
		this.freqUGen = freqUGen;
		constructPU();
	}

	/**
	 * Gets the cut-off frequency UGen.
	 * 
	 * @return The cut-off frequency UGen.
	 */
	public UGen getFreqUGen() {
		return freqUGen;
	}

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
}
