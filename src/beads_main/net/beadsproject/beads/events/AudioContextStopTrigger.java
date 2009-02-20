package net.beadsproject.beads.events;


import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;

// TODO: Auto-generated Javadoc
/**
 * The Class AudioContextStopTrigger.
 */
public class AudioContextStopTrigger extends Bead {

	/** The ac. */
	AudioContext ac;
	
	/**
	 * Instantiates a new audio context stop trigger.
	 * 
	 * @param ac
	 *            the ac
	 */
	public AudioContextStopTrigger(AudioContext ac) {
		this.ac = ac;
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.Bead#message(com.olliebown.beads.core.Bead)
	 */
	public void messageReceived(Bead message) {
		kill();
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.Bead#stop()
	 */
	public void kill() {
		ac.stop();
    }
	
}
