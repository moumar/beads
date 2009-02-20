package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


// TODO: Auto-generated Javadoc
/**
 * The Class Mult.
 */
public class Mult extends UGen {

	/**
	 * Instantiates a new mult.
	 * 
	 * @param context
	 *            the context
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public Mult(AudioContext context, UGen a, UGen b) {
		super(context, 2, 1);
		addInput(0, a, 0);
		addInput(1, b, 0);
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			bufOut[0][i] = bufIn[0][i] * bufIn[1][i];
		}
	}

}
