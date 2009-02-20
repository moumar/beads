/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;


// TODO: Auto-generated Javadoc
/**
 * The Class Static.
 */
public class Static extends UGen {

	/** The x. */
	public float x;
	
	/**
	 * Instantiates a new static.
	 * 
	 * @param ac
	 *            the ac
	 * @param x
	 *            the x
	 */
	public Static(AudioContext ac, float x) {
		super(ac, 0, 1);
		this.x = x;
		pause(true); //might as well be muted
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		// Do nothing
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#setValue(float)
	 */
	public void setValue(float value) {
		x = value;
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#getValue(int, int)
	 */
	public float getValue(int a, int b) {
		return x;	//whatever happens return x
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#getValue()
	 */
	public float getValue() {
		return x;
	}
	
	
}
