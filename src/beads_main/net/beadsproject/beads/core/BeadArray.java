package net.beadsproject.beads.core;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * An array of Beads (but also a subclass of Bead). Its
 * purpose is to forward messages to its array members. A BeadArray detects whether or not its members are deleted, and removes links to them if they are. For this reason it should be used in any situations where a Bead needs to be automatically disposed of. Note, however, that a BeadArray does not forward kill(), start() and pause() messages to its component Beads unless told to do so by setting the flags forwardKillCommand and forwardPauseCommand respectively. 
 * 
 * @author ollie
 */
public class BeadArray extends Bead {

	/** The beads. */
	private ArrayList<Bead> beads;
	private boolean forwardKillCommand;
	private boolean forwardPauseCommand;

	/**
	 * Creates an empty BeadArray.
	 */
	public BeadArray() {
		beads = new ArrayList<Bead>();
		forwardKillCommand = false;
		forwardPauseCommand = false;
	}

	/**
	 * Adds a new Bead to the list of receivers.
	 * 
	 * @param bead
	 *            Bead to add.
	 */
	public void add(Bead bead) {
		beads.add(bead);
	}

	/**
	 * Removes a Bead from the list of receivers.
	 * 
	 * @param bead
	 *            Bead to remove.
	 */
	public void remove(Bead bead) {
		beads.remove(bead);
	}

	/**
	 * Gets the ith Bead from the list of receivers.
	 * 
	 * @param i
	 *            index of Bead to retrieve.
	 * 
	 * @return the Bead at the ith index.
	 */
	public Bead get(int i) {
		return beads.get(i);
	}

	/**
	 * Clears the list of receivers.
	 */
	public void clear() {
		beads.clear();
	}

	/**
	 * Gets the size of the list of receivers.
	 * 
	 * @return size of list.
	 */
	public int size() {
		return beads.size();
	}
	
	/**
	 * Gets the contents of this BeadArrays as an ArrayList of Beads.
	 * 
	 * @return the beads.
	 */
	public ArrayList<Bead> getBeads() {
		return beads;
	}

	/**
	 * Forwards incoming message to all receivers.
	 * 
	 * @param message
	 *            incoming message.
	 */
	public void messageReceived(Bead message) {
		BeadArray clone = clone();
		for (int i = 0; i < clone.size(); i++) {
			Bead bead = clone.get(i);
			if (bead.isDeleted()) {
				remove(bead);
			} else {
				bead.message(message);
			}
		}
	}

	/**
	 * Creates a shallow copy of itself.
	 * 
	 * @return shallow copy of this Bead.
	 */
	public BeadArray clone() {
		BeadArray clone = new BeadArray();
		for (int i = 0; i < beads.size(); i++) {
			clone.add(beads.get(i));
		}
		return clone;
	}

	public boolean isForwardKillCommand() {
		return forwardKillCommand;
	}
	
	public void setForwardKillCommand(boolean forwardKillCommand) {
		this.forwardKillCommand = forwardKillCommand;
	}

	public boolean isForwardPauseCommand() {
		return forwardPauseCommand;
	}

	public void setForwardPauseCommand(boolean forwardPauseCommand) {
		this.forwardPauseCommand = forwardPauseCommand;
	}

	@Override
	public void kill() {
		super.kill();
		if(forwardKillCommand) {
			BeadArray clone = clone();
			for(Bead bead : clone.beads) {
				if (bead.isDeleted()) {
					remove(bead);
				} else {
					bead.kill();
				}
			}
		}
	}

	@Override
	public void pause(boolean paused) {
		super.pause(paused);
		if(forwardPauseCommand) {
			BeadArray clone = clone();
			for(Bead bead : clone.beads) {
				if (bead.isDeleted()) {
					remove(bead);
				} else {
					bead.pause(paused);
				}
			}
		}
	}

	@Override
	public void start() {
		super.start();
		if(forwardPauseCommand) {
			BeadArray clone = clone();
			for(Bead bead : clone.beads) {
				if (bead.isDeleted()) {
					remove(bead);
				} else {
					bead.start();
				}
			}
		}
	}
	
	

}
