package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;

/**
 * A simple allpass filter with variable delay. Implements the following formula:
 * 			Y(n) = X(n-d) + g * (Y(n-d) - X(n)),
 * 
 * 			for delay time <em>d</em> and factor <em>g</em>.
 * 
 * @author Benito Crawford
 * @version	0.9.1.1
 */
public class AllpassFilter extends UGen {

	protected float g;
	
	protected int maxDelay = 1, delay = 0, ind = 0, bufLen;
	protected UGen delayUGen, gUGen;
	protected float[] xn, yn;
	protected ParamUpdater pu;
	private int currsample;

	/**
	 * Constructor with delay and g specified by floats.
	 * 
	 * @param context	The AudioContext.
	 * @param maxdel	The maximum delay in samples; cannot be changed.
	 * @param idel		The initial delay in samples.
	 * @param ig		The initial g parameter.
	 */
	public AllpassFilter(AudioContext context, int maxdel, int idel, float ig) {
		super(context, 1, 1);
		maxDelay = Math.max(maxdel, 1);
		bufLen = maxDelay + 1;
		xn = new float[bufLen];
		yn = new float[bufLen];
		
		setDelay(idel);
		setG(ig);
	}

	/**
	 * Constructor with delay specified by a UGen and g specified by a float.
	 * 
	 * @param context	The AudioContext.
	 * @param maxdel	The maximum delay in samples; cannot be changed.
	 * @param idel		The delay UGen.
	 * @param ig		The initial g parameter.
	 */
	public AllpassFilter(AudioContext context, int maxdel, UGen idel, float ig) {
		super(context, 1, 1);
		maxDelay = Math.max(maxdel, 1);
		bufLen = maxDelay + 1;
		xn = new float[bufLen];
		yn = new float[bufLen];
		
		setDelay(idel);
		setG(ig);
	}

	/**
	 * Constructor with delay specified by a float and g specified by a UGen.
	 * 
	 * @param context	The AudioContext.
	 * @param maxdel	The maximum delay in samples; cannot be changed.
	 * @param idel		The initial delay in samples.
	 * @param ig		The g UGen.
	 */
	public AllpassFilter(AudioContext context, int maxdel, int idel, UGen ig) {
		super(context, 1, 1);
		maxDelay = Math.max(maxdel, 1);
		bufLen = maxDelay + 1;
		xn = new float[bufLen];
		yn = new float[bufLen];
		
		setDelay(idel);
		setG(ig);
	}
	
	/**
	 * Constructor with delay and g specified by UGens.
	 * 
	 * @param context	The AudioContext.
	 * @param maxdel	The maximum delay in samples; cannot be changed.
	 * @param idel		The delay UGen.
	 * @param ig		The g UGen.
	 */
	public AllpassFilter(AudioContext context, int maxdel, UGen idel, UGen ig) {
		super(context, 1, 1);
		maxDelay = Math.max(maxdel, 1);
		bufLen = maxDelay + 1;
		xn = new float[bufLen];
		yn = new float[bufLen];
			
		setDelay(idel);
		setG(ig);
	}

	protected void constructPU() {
		int c = 0;
		if(delayUGen != null) {
			c += 1;
		}
		if(gUGen != null) {
			c += 2;
		}
		if(pu == null || pu.type != c) {
			switch(c) {
			case 0:
				pu = new ParamUpdater(0);
				break;
				
			case 1:
				pu = new ParamUpdater(1) {
					void updateUGens() {
						delayUGen.update();
					}
					void updateParams() {
						int d = (int)delayUGen.getValue(0, currsample);
						if(d < 0) {
							delay = 0;
						}
						else if(d > maxDelay) {
							delay = maxDelay;
						}
						else {
							delay = d;
						}
					}
				};
				break;
				
			case 2:
				pu = new ParamUpdater(2) {
					void updateUGens() {
						gUGen.update();
					}
					void updateParams() {
						g = gUGen.getValue(0, currsample);
					}
				};
				break;
				
			case 3:
				pu = new ParamUpdater(3) {
					void updateUGens() {
						gUGen.update();
					}
					void updateParams() {
						int d = (int)delayUGen.getValue(0, currsample);
						if(d < 0) {
							delay = 0;
						}
						else if(d > maxDelay) {
							delay = maxDelay;
						}
						else {
							delay = d;
						}
						g = gUGen.getValue(0, currsample);
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
		
		pu.updateUGens();

		for (currsample = 0; currsample < bufferSize; currsample++) {
			pu.updateParams();
			
			int ind2 = (ind + bufLen - delay) % bufLen;
			bo[currsample] = yn[ind] = xn[ind2] + g * (yn[ind2] - (xn[ind] = bi[currsample]));
			ind = (ind + 1) % bufLen;
		}
	}
	
	protected class ParamUpdater {
		int type;
		ParamUpdater(int type) {
			this.type = type;
		}
		void updateUGens() {}
		void updateParams() {}
	}
	
	/**
	 * Gets the current g parameter.
	 * @return	The g parameter.
	 */
	public float getG() {
		return g;
	}
	
	/**
	 * Sets the g parameter. This clears the g Ugen if there is one.
	 * 
	 * @param g	The g parameter.
	 */
	public void setG(float g) {
		this.g = g;
		gUGen = null;
		constructPU();
	}
	
	/**
	 * Sets a UGen to determine the g value.
	 * 
	 * @param g	The g UGen.
	 */
	public void setG(UGen g) {
		gUGen = g;
		constructPU();
	}

	/**
	 * Gets the g UGen, if there is one.
	 * @return	The g UGen.
	 */
	public UGen getGUGen() {
		return gUGen;
	}
	
	/**
	 * Gets the current delay in samples.
	 * 
	 * @return	The delay in samples.
	 */
	public int getDelay() {
		return delay;
	}
	
	/**
	 * Sets the delay.
	 * 
	 * @param del	The delay in samples. This will remove the delay UGen if there is one.
	 */
	public void setDelay(int del) {
		if(del > maxDelay) { 
			delay = maxDelay;
		}
		else if(del < 0) {
			delay = 0;
		}
		else {
			delay = del;
		}
		delayUGen = null;
		constructPU();
	}
	
	/**
	 * Sets a UGen to determine the delay in samples. Delay times are converted to integers.
	 * @param del	The delay UGen.
	 */
	public void setDelay(UGen del) {
		delayUGen = del;
		constructPU();
	}
	
	/**
	 * Gets the delay UGen, if there is one.
	 * @return	The delay UGen.
	 */
	public UGen getDelayUGen() {
		return delayUGen;
	}
	
}
