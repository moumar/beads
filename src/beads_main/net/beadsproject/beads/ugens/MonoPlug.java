/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


/**
 * @author ollie
 * 
 * Really simple class to remap one output from one UGen.
 *
 */
public class MonoPlug extends UGen {

	public MonoPlug(AudioContext context, UGen sourceUGen, int outputIndex) {
		super(context, 1, 1);
		addInput(0, sourceUGen, outputIndex);
		bufOut[0] = bufIn[0];
	}

	@Override
	public void calculateBuffer() {}

}
