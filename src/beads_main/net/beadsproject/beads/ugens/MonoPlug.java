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
	 */
	public MonoPlug(AudioContext context) {
		super(context, 1, 1);
		outputInitializationRegime = OutputInitializationRegime.RETAIN;
		outputPauseRegime = OutputPauseRegime.ZERO;
	}

	/* (non-Javadoc)
	 * @see net.beadsproject.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		bufOut[0] = bufIn[0];
	}
	
	public static void main(String[] args) {
		AudioContext ac = new AudioContext();
		WavePlayer wp = new WavePlayer(ac, 500f, Buffer.SINE);
		
		MonoPlug mp = new MonoPlug(ac);
		mp.addInput(wp);
		ac.out.addInput(mp);
		
//		ac.out.addInput(wp);
		ac.start();
	}

}
