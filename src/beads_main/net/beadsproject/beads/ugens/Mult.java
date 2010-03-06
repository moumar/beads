/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Mult multiplies two {@link UGen}s together.
 * 
 * @beads.category utilities
 * @author ollie
 */
public class Mult extends UGen {

	/**
	 * Instantiates a new Mult with the two {@link UGen}s whose signals will be multiplied.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param a
	 *            one UGen.
	 * @param b
	 *            another UGen.
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
