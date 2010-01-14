package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.*;
//import net.beadsproject.beads.ugens.Delta;

/**
 * Outputs 1 if the input signal is increasing; -1 if it is decreasing; 0 if it
 * is the same. Use {@link Delta} to find how much a signal is changing.
 * 
 * @author Benito Crawford
 * @version 0.9
 */
public class Change extends UGen {

	private float lastX = 0;

	/**
	 * Bare constructor.
	 * 
	 * @param context
	 *            The audio context.
	 */
	public Change(AudioContext context) {
		super(context, 1, 1);
	}

	/**
	 * Constructor for a given input UGen.
	 * 
	 * @param context
	 *            The audio context.
	 * @param ugen
	 *            The input UGen.
	 */
	public Change(AudioContext context, UGen ugen) {
		super(context, 1, 1);
		addInput(0, ugen, 0);
	}

	@Override
	public void calculateBuffer() {

		float[] bi = bufIn[0];
		float[] bo = bufOut[0];
		float x;

		for (int i = 1; i < bufferSize; i++) {
			if ((x = bi[i]) > lastX) {
				bo[i] = 1;
			} else if (x < lastX) {
				bo[i] = -1;
			} else {
				bo[i] = 0;
			}
			lastX = x;
		}
	}
}
