package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.data.*;

/**
 * A simple 2nd-order resonant low-pass filter optimized for single-channel
 * processing. Faster than a BiquadFilter because its algorithm is of the form:
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
 * @author Benito Crawford
 * @version 0.9.5
 */
public class LPRezFilter extends UGen implements DataBeadReceiver {

	protected float freq = 100;
	protected float res = .5f, _2pi_over_sr, cosw = 0;
	protected UGen freqUGen, resUGen;

	protected float a1, a2, b0;
	private float y1 = 0, y2 = 0;

	protected boolean isFreqStatic, isResStatic;

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
		setFreq(freq).setRes(res);
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
		setFreq(freq).setRes(res);
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
		setFreq(freq).setRes(res);
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
		setFreq(freq).setRes(res);
	}

	/**
	 * Constructor for multi-channel processing; used by LPRezFilterMulti
	 * 
	 * @param con
	 *            The audio context.
	 * @param channels
	 *            The number of channels.
	 */
	protected LPRezFilter(AudioContext con, int channels) {
		super(con, channels, channels);
		_2pi_over_sr = (float) (2 * Math.PI / con.getSampleRate());
		setFreq(freq).setRes(res);
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

		if (isFreqStatic && isResStatic) {

			bo[0] = bi[0] * b0 - a1 * y1 - a2 * y2;
			bo[1] = bi[1] * b0 - a1 * bo[0] - a2 * y1;

			// main loop
			for (int currsamp = 2; currsamp < bufferSize; currsamp++) {
				bo[currsamp] = bi[currsamp] * b0 - a1 * bo[currsamp - 1] - a2
						* bo[currsamp - 2];
			}

		} else {

			freqUGen.update();
			resUGen.update();

			cosw = (float) (Math.cos(_2pi_over_sr
					* (freq = freqUGen.getValue(0, 0))));
			if ((res = resUGen.getValue(0, 0)) > .999999f) {
				res = .999999f;
			} else if (res < 0) {
				res = 0;
			}
			calcVals();
			bo[0] = bi[0] * b0 - a1 * y1 - a2 * y2;

			cosw = (float) (Math.cos(_2pi_over_sr
					* (freq = freqUGen.getValue(0, 1))));
			if ((res = resUGen.getValue(0, 1)) > .999999f) {
				res = .999999f;
			} else if (res < 0) {
				res = 0;
			}
			calcVals();
			bo[1] = bi[1] * b0 - a1 * bo[0] - a2 * y1;

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

				bo[currsamp] = bi[currsamp] * b0 - a1 * bo[currsamp - 1] - a2
						* bo[currsamp - 2];
			}

		}

		y2 = bo[bufferSize - 2];
		if (Float.isNaN(y1 = bo[bufferSize - 1])) {
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
	 * @return This filter instance.
	 */
	public LPRezFilter setFreq(float f) {
		freq = f;
		if (isFreqStatic) {
			freqUGen.setValue(f);
		} else {
			freqUGen = new Static(context, f);
			isFreqStatic = true;
		}
		cosw = (float) (Math.cos(_2pi_over_sr * freq));
		calcVals();
		return this;
	}

	/**
	 * Sets a UGen to specify the cut-off frequency. Passing a null value
	 * freezes the parameter.
	 * 
	 * @param f
	 *            The frequency UGen.
	 * @return This filter instance.
	 */
	public LPRezFilter setFreq(UGen f) {
		if (f == null) {
			setFreq(freq);
		} else {
			freqUGen = f;
			f.update();
			freq = f.getValue();
		}
		return this;
	}

	/**
	 * Gets the frequency UGen, if it exists.
	 * 
	 * @return The frequency UGen.
	 */
	public UGen getFreqUGen() {
		if (isFreqStatic) {
			return null;
		} else {
			return freqUGen;
		}
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
	 * @return This filter instance.
	 */
	public LPRezFilter setRes(float r) {
		if (r > .999999f) {
			res = .999999f;
		} else if (r < 0) {
			res = 0;
		} else {
			res = r;
		}
		if (isResStatic) {
			resUGen.setValue(res);
		} else {
			resUGen = new Static(context, res);
			isResStatic = true;
		}
		calcVals();
		return this;
	}

	/**
	 * Sets a UGen to specify the filter resonance. Passing a null value freezes
	 * the parameter.
	 * 
	 * @param r
	 *            The resonance UGen.
	 * @return This filter instance.
	 */
	public LPRezFilter setRes(UGen r) {
		if (r == null) {
			setRes(res);
		} else {
			resUGen = r;
			r.update();
			res = r.getValue();
			isResStatic = false;
		}
		return this;
	}

	/**
	 * Gets the resonance UGen, if it exists.
	 * 
	 * @return The resonance UGen.
	 */
	public UGen getResUGen() {
		if (isResStatic) {
			return null;
		} else {
			return resUGen;
		}
	}

	/**
	 * Sets the filter parameters with a DataBead.
	 * <p>
	 * Use the following properties to specify filter parameters:
	 * </p>
	 * <ul>
	 * <li>"frequency": (float or UGen)</li>
	 * <li>"resonance": (float or UGen)</li>
	 * </ul>
	 * 
	 * @param paramBead
	 *            The DataBead specifying parameters.
	 * @return This filter instance.
	 */
	public LPRezFilter setParams(DataBead paramBead) {
		if (paramBead != null) {
			Object o;

			if ((o = paramBead.get("frequency")) != null) {
				if (o instanceof UGen) {
					setFreq((UGen) o);
				} else {
					setFreq(paramBead.getFloat("frequency", freq));
				}
			}

			if ((o = paramBead.get("resonance")) != null) {
				if (o instanceof UGen) {
					setRes((UGen) o);
				} else {
					setRes(paramBead.getFloat("resonance", res));
				}
			}

		}
		return this;
	}

	public void messageReceived(Bead message) {
		if (message instanceof DataBead) {
			setParams((DataBead) message);
		}
	}

	/**
	 * Gets a DataBead with properties "frequency" and "resonance" set to the
	 * corresponding filter parameters.
	 * 
	 * @return The parameter DataBead.
	 */
	public DataBead getParams() {
		DataBead db = new DataBead();
		if (isFreqStatic) {
			db.put("frequency", freq);
		} else {
			db.put("frequency", freqUGen);
		}

		if (isResStatic) {
			db.put("resonance", res);
		} else {
			db.put("resonance", resUGen);
		}

		return db;
	}

	/**
	 * Gets a DataBead with properties "frequency" and "resonance" set to static
	 * float values corresponding to the current filter parameters.
	 * 
	 * @return The static parameter DataBead.
	 */
	public DataBead getStaticParams() {
		DataBead db = new DataBead();
		db.put("frequency", freq);
		db.put("resonance", res);
		return db;
	}

	/**
	 * Sets the filter's parameters with properties from a DataBead.
	 * @see #setParams(DataBead)
	 */
	public DataBeadReceiver sendData(DataBead db) {
		setParams(db);
		return this;
	}

}
