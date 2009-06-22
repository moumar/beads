/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * MonoPlug performs the simple task of channelling a single output from a multi-channel {@link UGen}.
 * 
 * @beads.category utilities
 * @author ollie
 */
public class MonoPlug extends UGen {

	/**
	 * Instantiates a new MonoPlug.
	 * 
	 * @param context the AudioContext.
	 * @param sourceUGen the source UGen.
	 * @param outputIndex the output index of the source UGen.
	 */
	public MonoPlug(AudioContext context, UGen sourceUGen, int outputIndex) {
		super(context, 1, 1);
		addInput(0, sourceUGen, outputIndex);
		bufOut[0] = bufIn[0];
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {}

}
