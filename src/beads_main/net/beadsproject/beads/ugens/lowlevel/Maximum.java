package net.beadsproject.beads.ugens.lowlevel;

import net.beadsproject.beads.core.*;
import net.beadsproject.beads.ugens.core.Static;

/**
 * For each sample, outputs the maximum of its two inputs.
 * 
 * @author Benito Crawford
 * @version 0.9
 */
public class Maximum extends UGen {

	/**
	 * Constructor with no assigned inputs.
	 * 
	 * @param context
	 *            The audio context.
	 */
	public Maximum(AudioContext context) {
		super(context, 2, 1);
	}

	/**
	 * Constructor for 1 UGen input and a static maximum value.
	 * 
	 * @param context
	 *            The audio context.
	 * @param ugen
	 *            The input UGen.
	 * @param maxVal
	 *            The minimum value.
	 */
	public Maximum(AudioContext context, UGen ugen, float maxVal) {
		super(context, 2, 1);
		addInput(0, ugen, 0);
		addInput(1, new Static(context, maxVal), 0);
	}

	/**
	 * Constructor for 2 UGen inputs.
	 * 
	 * @param context
	 *            The AudioContext.
	 * @param ugen1
	 *            The first UGen input.
	 * @param ugen2
	 *            The second UGen input.
	 */
	public Maximum(AudioContext context, UGen ugen1, UGen ugen2) {
		super(context, 2, 1);
		addInput(0, ugen1, 0);
		addInput(1, ugen2, 0);
	}

	@Override
	public void calculateBuffer() {
		float[] bi1 = bufIn[0];
		float[] bi2 = bufIn[1];
		float[] bo = bufOut[0];

		for (int i = 0; i < bufferSize; i++) {
			if (bi1[i] > bi2[i]) {
				bo[i] = bi1[i];
			} else {
				bo[i] = bi2[i];
			}
		}

	}

}
