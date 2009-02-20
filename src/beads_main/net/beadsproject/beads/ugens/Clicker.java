package net.beadsproject.beads.ugens;

import java.util.Arrays;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

// TODO: Auto-generated Javadoc
/**
 * A simple Click UGen which makes one click and then stops.
 */
public class Clicker extends UGen {

	private boolean done;
	private float strength;
	
	/**
	 * Instantiates a new clicker.
	 * 
	 * @param context
	 *            the context
	 */
	public Clicker(AudioContext context, float strength) {
		super(context, 0, 1);
		this.strength = strength;
		done = false;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if(done) kill();
		else {
			bufOut[0][0] = strength;
			done = true;
		}
	}

}