/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import java.util.Random;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Noise generates white noise.
 * 
 * @author ollie
 */
public class Noise extends UGen {

	/** The random number generator used to generate the noise. */
	private Random rng;
	
	/**
	 * Instantiates a new Noise.
	 * 
	 * @param context the AudioContext.
	 */
	public Noise(AudioContext context) {
		super(context, 1);
		rng = new Random();
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < bufferSize; i++) {
			bufOut[0][i] = rng.nextFloat();
		}
	}

}
