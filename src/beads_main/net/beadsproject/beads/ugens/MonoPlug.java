/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;

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
		outputInitializationRegime = OutputInitializationRegime.RETAIN;
		outputPauseRegime = OutputPauseRegime.ZERO;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {}
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f, Buffer.SINE);
		
		MonoPlug mp = new MonoPlug(ac, wp, 0);
		ac.out.addInput(mp);
		
		ac.start();
	}

}
