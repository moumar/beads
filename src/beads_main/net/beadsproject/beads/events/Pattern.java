/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.events;

import java.util.Hashtable;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;

/**
 * A Pattern is a {@link Bead} that responds to integer events by 
 * generating other integer events and forwarding them to a {@link BeadArray} 
 * of listeners. Typically, Patterns are used with {@link net.beadsproject.beads.ugens.Clock Clocks}. 
 * 
 * <p/>Patterns contain a list of events specified as key/value pairs of integers. A Pattern keeps an internal counter which is incremented internally. When the counter is incremented, if its new value is listed as a key, Pattern forwards the corresponding value to its listeners. 
 * 
 * Pattern responds to {@link Bead} messages that implement {@link IntegerBead}. An incoming integer causes Pattern's internal counter to increment if it is a multiple of {@link #hop}. If the internal counter reaches {@link Pattern#loop}, it returns to zero. In this 
 * way, Pattern can be quicly maniuplated to play back at different speeds and loop lengths in 
 * response to a regular {@link net.beadsproject.beads.ugens.Clock Clock}. 
 */
public class Pattern extends Bead implements IntegerBead {
	
    /** A list of events. */
    private final Hashtable<Integer, Integer> events;
    
    /** A BeadArray which is notified for each event. */
    private final BeadArray listeners;
    
    /** The integer hop. */
    private int hop;
    
    /** The integer loop. */
    private int loop;
    
    /** The current index. */
    private int currentIndex;
    
    /** The current value. */
    private Integer currentValue;
    
    /**
     * Instantiates a new empty pattern.
     */
    public Pattern() {
    	listeners = new BeadArray();
        events = new Hashtable<Integer, Integer>();
        setNoLoop();
        setHop(1);
        reset();
    }
    
    /**
     * Resets the pattern's current index to zero.
     */
    public void reset() {
    	currentIndex = 0;
    }
    
    /**
     * Adds a listener.
     * 
     * @param listener the new listener.
     */
    public void addListener(Bead listener) {
    	listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * 
     * @param listener the listener.
     */
    public void removeListener(Bead listener) {
    	listeners.remove(listener);
    }
    
    /**
     * Adds an event consisting of a integer key and an integer value.
     * 
     * @param key the key.
     * @param value the value.
     */
    public void addEvent(int key, int value) {
        events.put(key, value);
    }
    
    /**
     * Removes the event with the given integer key.
     * 
     * @param key the key.
     */
    public void removeEvent(int key) {
    	for(int index : events.keySet()) {
            if(index == key) {
                events.remove(index);
                break;
            }
        }
    }
    
    /**
     * Clears the pattern data. Does not reset the Pattern.
     */
    public void clear() {
    	events.clear();
    }

    /**
     * Handles a message. The message argument must implement {@link IntegerBead}. Checks to see if it should do anything for the given integer, and forwards any resulting integer to its listeners.
     * @see #message(Bead)
     */
    public void messageReceived(Bead message) {
        int index = ((IntegerBead)message).getInt();
        getEventAtIndex(index);
        if(currentValue != null) {
    		listeners.message(this);
    	}
    }
    
    /**
     * Gets the event at the given integer index.
     * 
     * @param index the index.
     * 
     * @return the event at this index, or null if no event exists.
     */
    public Integer getEventAtIndex(int index) {
    	currentValue = null;
    	if(index % hop == 0) {
    		currentValue = events.get(currentIndex);
    		currentIndex++;
    		if(currentIndex >= loop) reset();
    	}
        return currentValue;
    }

    /**
     * Gets the loop length.
     * 
     * @return the loop length.
     */
    public int getLoop() {
        return loop;
    }

    /**
     * Sets the loop length and activates loop mode.
     * 
     * @param loop the loop length.
     */
    public void setLoop(int loop) {
        this.loop = loop;
    }
    
    /**
     * Deactivates loop mode.
     */
    public void setNoLoop() {
    	loop = Integer.MAX_VALUE;
    }

	/**
	 * Gets the hop size.
	 * 
	 * @return the hop size.
	 */
	public int getHop() {
		return hop;
	}

	/**
	 * Sets the hop size.
	 * 
	 * @param hop the hop size.
	 */
	public void setHop(int hop) {
		this.hop = hop;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.events.IntegerBead#getInt()
	 */
	public int getInt() {
		return currentValue;
	}
	
	/**
	 * Gets the current value.
	 * 
	 * @return the current value.
	 */
	public int getValue() {
		return currentValue;
	}
    
}
