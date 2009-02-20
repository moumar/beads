package net.beadsproject.beads.core;

import javax.swing.JPanel;

// TODO: Auto-generated Javadoc
/**
 * Bead is an abstract class which defines basic behaviour such as starting and
 * stopping, pausing and handling messages. A Bead can send a message to another Bead using the message() method. Beads handle these messages by subclassing the messageReceived() method. {@link #BeadArray} can be used to organise Beads into groups such that if a Bead gets deleted it will be removed from the array automatically. When a Bead is paused, it stops receiving messages. Beads are not paused by default. The main subclass of Bead is {@link #UGen},
 * which is where most of the action happens. When a UGen is paused, it stops passing audio to its outputs. When a Bead is killed a deleted flag is switched on. Killed Beads will then be removed from any BeadArrays, and Killed UGens will be deleted from any signal chains. You can add another Bead to this Bead as a killListener, and
 * 
 * @author ollie
 */
public abstract class Bead {

	/** The paused. */
	private boolean paused;
	
	/** The deleted. */
	private boolean deleted;
	
	/** The kill listener. */
	private Bead killListener;
	
	/** The name. */
	private String name;

	
	/**
	 * Instantiates a new bead.
	 */
	public Bead() {
		 paused = false;
		 deleted = false;
		 killListener = null;
		 name = null;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the class of the Bead as a String.
	 * 
	 * @return String denoting the class of the Bead.
	 */
	@Override
	public String toString() {
		return getClass().toString() + " " + "name=" + name;
	}

	/**
	 * Send this Bead a message.
	 * 
	 * @param message the Bead is the message.
	 */
	public final void message(Bead message) {
		if(!paused) messageReceived(message);
	}
	
	/**
	 * Subclasses should override this in order to handle incoming messages. Typically a Bead would send a message to another Bead with itself as the arugment.
	 * 
	 * @param message the message
	 */
	public void messageReceived(Bead message) {
	}
	
	/**
	 * Equivalent to pause(false).
	 */
	public void start() {
		paused = false;
	}

	/**
	 * Stops this Bead, and sets its state to deleted if this Bead is
	 * self-deleting. Means different things for different subclasses of Bead.
	 */
	public void kill() {
		deleted = true;
		if(killListener != null) {
			killListener.message(this);
		}
	}
	
	/**
	 * Checks if is paused.
	 * 
	 * @return true, if is paused
	 */
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Pause.
	 * 
	 * @param paused the paused
	 */
	public void pause(boolean paused) {
		this.paused = paused;
	}
	
	/**
	 * Sets the kill listener.
	 * 
	 * @param killListener the new kill listener
	 */
	public void setKillListener(Bead killListener) {
		this.killListener = killListener;
	}
	
    /**
     * Gets the kill listener.
     * 
     * @return the kill listener
     */
    public Bead getKillListener() {
    	return killListener;
    }


	/**
	 * Determines if this Bead is deleted.
	 * 
	 * @return true if this Bead's state is deleted, false otherwise.
	 */
	public boolean isDeleted() {
		return deleted;
	}

}
