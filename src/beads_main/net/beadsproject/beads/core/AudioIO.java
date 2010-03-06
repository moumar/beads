package net.beadsproject.beads.core;

/**
 * AudioIO is the abstract base class for setting up interaction between AudioContext and the world. It is 
 * designed to be largely controlled by AudioContext. To be precise, AudioContext will create(), start(), stop() and destroy() the
 * AudioIO it is initialised with. However, certain AudioIO implementations may need to be set up before being passed to AudioContext(). By default, AudioContext creates a JavaSoundAudioContext.
 * @author ollie
 *
 */
public abstract class AudioIO {

	protected AudioContext context;
	
	protected boolean prepare() {return true;}
	protected abstract boolean create();
	protected abstract boolean start();
	protected boolean stop() {return true;}
	protected abstract boolean destroy();
	protected abstract UGen getAudioInput(int[] channels);
	
	protected boolean update() {
		return context.update();
	}
	
	public AudioContext getContext() {
		return context;
	}

}
