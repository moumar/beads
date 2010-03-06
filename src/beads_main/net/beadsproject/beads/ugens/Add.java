/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Adds two {@link UGen} outputs together. In most cases this is more easily and efficiently achieved by just plugging two output connections into the same input, in which case the signals get added automatically.
 *
 * @beads.category utilities
 * @author ollie
 */
public class Add extends UGen {

	/**
	 * Instantiates a new Add UGen with UGen a and UGen b added together.
	 * 
	 * @param context the AudioContext.
	 * @param a the first UGen. 
	 * @param b the second UGen.
	 */
	public Add(AudioContext context, UGen a, UGen b) {
		this(context, Math.min(a.getOuts(), b.getOuts()));
		addInput(a);
		addInput(b);
	}
	
	
	/**
	 * Instantiates a new Add UGen without any UGens connected to it.
	 * 
	 * @param context the AudioContext.
	 * @param inouts the number of inputs (= the number of outputs).
	 */
	public Add(AudioContext context, int inouts) {
		super(context, inouts, inouts);
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		for(int i = 0; i < outs; i++) {
			for(int j = 0; j < bufferSize; j++) {
				bufOut[i][j] = bufIn[i][j];
			}
		}
	}
	
}
