package net.beadsproject.beads.events;

import net.beadsproject.beads.core.Bead;

// TODO: Auto-generated Javadoc
/**
 * The Class StopTrigger.
 */
public class PlayTrigger extends Bead {

	/** The receiver. */
	Bead receiver;
	
	/**
	 * Instantiates a new stop trigger.
	 * 
	 * @param receiver
	 *            the receiver
	 */
	public PlayTrigger(Bead receiver) {
		this.receiver = receiver;
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.Bead#message(com.olliebown.beads.core.Bead)
	 */
	public void messageReceived(Bead message) {
		if(receiver != null) receiver.pause(false);
	}
}
