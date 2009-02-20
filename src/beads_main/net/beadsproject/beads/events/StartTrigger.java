package net.beadsproject.beads.events;

import net.beadsproject.beads.core.Bead;

// TODO: Auto-generated Javadoc
/**
 * The Class StartTrigger.
 */
public class StartTrigger extends Bead {

	/** The receiver. */
	Bead receiver;
	
	/**
	 * Instantiates a new start trigger.
	 * 
	 * @param receiver
	 *            the receiver
	 */
	public StartTrigger(Bead receiver) {
		this.receiver = receiver;
	}
	
	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.Bead#message(com.olliebown.beads.core.Bead)
	 */
	public void messageReceived(Bead message) {
		receiver.start();
		
	}
}