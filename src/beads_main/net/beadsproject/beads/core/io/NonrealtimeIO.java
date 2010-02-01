package net.beadsproject.beads.core.io;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;

/**
 * A dummy AudioIO class that is purely for non-realtime use; it does not
 * interface with any system audio.
 * 
 * @author Benito Crawford
 * @version 0.9.5
 * 
 */
public class NonrealtimeIO extends AudioIO {

	protected boolean create() {
		return true;
	}

	protected boolean start() {
		return false;
	}

	protected boolean destroy() {
		return true;
	}

	protected UGen getAudioInput(int[] channels) {
		return null;
	}

}
