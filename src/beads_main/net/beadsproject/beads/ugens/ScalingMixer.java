/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalingMixer.
 */
public class ScalingMixer extends UGen {

	/**
	 * Instantiates a new scaling mixer.
	 * 
	 * @param player
	 *            the player
	 */
	public ScalingMixer(AudioContext player) {
        this(player, 1);
    }
	
    /**
	 * Instantiates a new scaling mixer.
	 * 
	 * @param player
	 *            the player
	 * @param inouts
	 *            the inouts
	 */
    public ScalingMixer(AudioContext player, int inouts) {
        super(player, inouts, inouts);
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
        for(int i = 0; i < ins; i++) {
            int numInputs = getNumberOfConnectedUGens(i);
	        for(int j = 0; j < bufferSize; j++) {
	            bufOut[i][j] = bufIn[i][j] / (float)numInputs;
	            //System.out.println(bufIn[0][i] + " " + bufOut[0][i] + " " + numInputs);
	        }
        }
    }

}
