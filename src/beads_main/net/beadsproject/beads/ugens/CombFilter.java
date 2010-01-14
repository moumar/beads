package net.beadsproject.beads.ugens;

import java.util.Arrays;

import net.beadsproject.beads.core.*;

/**
 * Implements a simple comb filter with both feed-forward and feed-back
 * components.
 * <p>
 * y(n) = a * x(n) + g * x(n - d) - h * y(n - d)
 * 
 * @author Benito Crawford
 * @version 0.9.1
 */
public class CombFilter extends UGen {

	protected float a = 0, g = 0, h = 0;
	protected int maxDelay = 1, delay = 0, ind = 0;
	protected UGen aUGen, gUGen, hUGen, delayUGen;
	protected float[] xn, yn;
	protected ParamUpdater pu1, pu2;
	private int currsample;
	protected int bufLen = 1;

	/**
	 * Constructor.
	 * 
	 * @param con
	 *            The audio context.
	 * @param maxdel
	 *            The maximum delay in samples.
	 */
	public CombFilter(AudioContext con, int maxdel) {
		super(con, 1, 1);
		maxDelay = Math.max(maxdel, 1);
		bufLen = maxDelay + 1;
		yn = new float[bufLen];
		xn = new float[bufLen];
		constructPUs();
	}

	/**
	 * Set up our ParamUpdaters.
	 */
	protected void constructPUs() {
		int c = 0;
		if (delayUGen != null) {
			c += 1;
		}
		if (gUGen != null) {
			c += 2;
		}
		if (pu1 == null || pu1.type != c) {
			switch (c) {
			case 0:
				pu1 = new ParamUpdater(c);
				break;
			case 1:
				pu1 = new ParamUpdater(c) {
					void updateUGens() {
						delayUGen.update();
					}

					void updateParams() {
						int d = (int) delayUGen.getValue(0, currsample);
						if (d < 0) {
							delay = 0;
						} else if (d >= maxDelay) {
							delay = maxDelay;
						} else {
							delay = d;
						}
					}
				};
				break;
			case 2:
				pu1 = new ParamUpdater(c) {
					void updateUGens() {
						gUGen.update();
					}

					void updateParams() {
						g = gUGen.getValue(0, currsample);
					}
				};
				break;
			case 3:
				pu1 = new ParamUpdater(c) {
					void updateUGens() {
						delayUGen.update();
						gUGen.update();
					}

					void updateParams() {
						int d = (int) delayUGen.getValue(0, currsample);
						if (d < 0) {
							delay = 0;
						} else if (d >= maxDelay) {
							delay = maxDelay;
						} else {
							delay = d;
						}
						g = gUGen.getValue(0, currsample);
					}
				};
				break;
			}
		}

		c = 0;
		if (hUGen != null) {
			c += 1;
		}
		if (aUGen != null) {
			c += 2;
		}

		if (pu2 == null || pu2.type != c) {
			switch (c) {
			case 0:
				pu2 = new ParamUpdater(0);
				break;
			case 1:
				pu2 = new ParamUpdater(1) {
					void updateUGens() {
						hUGen.update();
					}

					void updateParams() {
						h = hUGen.getValue(0, currsample);
					}
				};
				break;
			case 2:
				pu2 = new ParamUpdater(2) {
					void updateUGens() {
						aUGen.update();
					}

					void updateParams() {
						a = aUGen.getValue(0, currsample);
					}
				};
				break;
			case 3:
				pu2 = new ParamUpdater(3) {
					void updateUGens() {
						hUGen.update();
						aUGen.update();
					}

					void updateParams() {
						h = hUGen.getValue(0, currsample);
						a = aUGen.getValue(0, currsample);
					}
				};
				break;
			}
		}

	}

	@Override
	public void calculateBuffer() {

		float[] bi = bufIn[0];
		float[] bo = bufOut[0];
		pu1.updateUGens();
		pu2.updateUGens();

		for (currsample = 0; currsample < bufferSize; currsample++) {
			pu1.updateParams();
			pu2.updateParams();
			int ind2 = (ind + bufLen - delay) % bufLen;
			bo[currsample] = yn[ind] = a * (xn[ind] = bi[currsample]) + g
					* xn[ind2] - h * yn[ind2];
			ind = (ind + 1) % bufLen;
		}
	}

	/**
	 * Use this to reset the filter if it explodes.
	 */
	public void reset() {
		Arrays.fill(yn, 0);
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
	 * Gets the maximum delay in samples.
	 * 
	 * @return The maximum delay.
	 */
	public int getMaxDelay() {
		return maxDelay;
	}

	/**
	 * Gets the current delay in samples.
	 * 
	 * @return The delay in samples.
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Sets the delay time in samples. This will remove the delay UGen, if there
	 * is one.
	 * 
	 * @param delay
	 *            The delay in samples.
	 */
	public void setDelay(int delay) {
		if (delay < 0) {
			this.delay = 0;
		} else if (delay >= maxDelay) {
			this.delay = maxDelay;
		} else {
			this.delay = delay;
		}
		delayUGen = null;
		constructPUs();
	}

	/**
	 * Sets a UGen to specify the delay in samples (converted to ints).
	 * 
	 * @param delay
	 *            The delay UGen.
	 */
	public void setDelay(UGen delay) {
		delayUGen = delay;
		constructPUs();
	}

	/**
	 * Gets the delay UGen, if it exists.
	 * 
	 * @return The delay UGen.
	 */
	public UGen getDelayUGen() {
		return delayUGen;
	}

	/**
	 * Gets the g parameter.
	 * 
	 * @return The g parameter.
	 */
	public float getG() {
		return g;
	}

	/**
	 * Sets the g parameter to a float value. This will remove the g UGen, if
	 * there is one.
	 * 
	 * @param g
	 *            The g parameter.
	 */
	public void setG(float g) {
		this.g = g;
		gUGen = null;
		constructPUs();
	}

	/**
	 * Sets a UGen to specify the g parameter.
	 * 
	 * @param g
	 *            The g UGen.
	 */
	public void setG(UGen g) {
		gUGen = g;
		constructPUs();
	}

	/**
	 * Gets the g UGen, if it exists.
	 * 
	 * @return The g UGen.
	 */
	public UGen getGUGen() {
		return gUGen;
	}

	/**
	 * Gets the h parameter.
	 * 
	 * @return The h parameter.
	 */
	public float getH() {
		return h;
	}

	/**
	 * Sets the h parameter to a float value. This will remove the h UGen, if
	 * there is one.
	 * 
	 * @param h
	 *            The h parameter.
	 */
	public void setH(float h) {
		this.h = h;
		hUGen = null;
		constructPUs();
	}

	/**
	 * Sets a UGen to specify the h parameter.
	 * 
	 * @param h
	 *            The h UGen.
	 */
	public void setH(UGen h) {
		hUGen = h;
		constructPUs();
	}

	/**
	 * Gets the h UGen, if it exists.
	 * 
	 * @return The h UGen.
	 */
	public UGen getHUGen() {
		return hUGen;
	}

	/**
	 * Gets the h parameter.
	 * 
	 * @return The h parameter.
	 */
	public float getA() {
		return a;
	}

	/**
	 * Sets the 'a' parameter to a float value. This will remove the 'a' UGen,
	 * if there is one.
	 * 
	 * @param a
	 *            The 'a' parameter.
	 */
	public void setA(float a) {
		this.a = a;
		hUGen = null;
		constructPUs();
	}

	/**
	 * Sets a UGen to specify the 'a' parameter.
	 * 
	 * @param a
	 *            The 'a' UGen.
	 */
	public void setA(UGen a) {
		aUGen = a;
		constructPUs();
	}

	/**
	 * Gets the 'a' UGen, if it exists.
	 * 
	 * @return The 'a' UGen.
	 */
	public UGen getAUGen() {
		return aUGen;
	}

	/**
	 * Sets all the parameters at once. This will clear parameter UGens, if they
	 * exist.
	 * 
	 * @param delay
	 *            The delay in samples.
	 * @param a
	 *            The 'a' parameter.
	 * @param g
	 *            The g parameter.
	 * @param h
	 *            The h parameter.
	 */
	public void setParams(int delay, float a, float g, float h) {
		this.a = a;
		this.g = g;
		this.h = h;
		aUGen = null;
		gUGen = null;
		hUGen = null;
		setDelay(delay);
	}

	/**
	 * Sets the parameter UGens. Passing null values will freeze the parameters
	 * at their previous values.
	 * 
	 * @param delUGen
	 *            The delay UGen.
	 * @param aUGen
	 *            The 'a' UGen.
	 * @param gUGen
	 *            The g UGen.
	 * @param hUGen
	 *            The h UGen.
	 */
	public void setParams(UGen delUGen, UGen aUGen, UGen gUGen, UGen hUGen) {
		delayUGen = delUGen;
		this.aUGen = aUGen;
		this.gUGen = gUGen;
		this.hUGen = hUGen;
		constructPUs();
	}
	
	public static void main(String[] args) {
		//Ollie - I'm interested in comparing the speed of this ParamUpdater with Static
		AudioContext ac = new AudioContext();
		for(int i = 0; i < 1000; i++) {
			CombFilter c = new CombFilter(ac, 1000);
			
			//compare these two lines...
//			c.setA(1f);
			c.setA(new Static(ac, 1f));
			
			ac.out.addInput(c);
		}
		ac.start();
	}
}
