package net.beadsproject.beads.core.io;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;

/**
 * AudioIO is the abstract base class for setting up interaction between
 * AudioContext and the world. It is designed to be largely controlled by
 * AudioContext. To be precise, AudioContext will create(), start(), stop() and
 * destroy() the AudioIO it is initialised with. However, certain AudioIO
 * implementations may need to be set up before being passed to AudioContext().
 * By default, AudioContext creates a JavaSoundAudioContext.
 * 
 * @author ollie
 * 
 */
public class NonrealtimeIO extends AudioIO {

	protected boolean create() {
		return true;
	}

	protected boolean start() {
		return false;
	}

	protected boolean destroy(){
		return true;
	}

	protected UGen getAudioInput(int[] channels) {
		return null;
	}

}
