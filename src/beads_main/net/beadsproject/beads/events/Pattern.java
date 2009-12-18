/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

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
public class Pattern extends Bead implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static enum ContinuousPlayMode {INTERNAL, EXTERNAL};

	/** A list of events. */
    private final Hashtable<Integer, ArrayList<Integer>> events;
    
    /** A BeadArray which is notified for each event. */
    private final transient BeadArray listeners;
    
    /** The integer hop. */
    private int hop;
    
    /** The integer loop. */
    private int loop;
    
    /** The current index. */
    private int currentIndex;
    
    /** The current value. */
    private ArrayList<Integer> currentValue;
    
    private ContinuousPlayMode continuousPlayMode;
    
    /**
     * Instantiates a new empty pattern.
     */
    public Pattern() {
    	listeners = new BeadArray();
        events = new Hashtable<Integer, ArrayList<Integer>>();
        setNoLoop();
        setHop(1);
        reset();
        continuousPlayMode = ContinuousPlayMode.EXTERNAL;
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
    
    
    
    public ContinuousPlayMode getContinuousPlayMode() {
		return continuousPlayMode;
	}

	public void setContinuousPlayMode(ContinuousPlayMode continuousPlayMode) {
		this.continuousPlayMode = continuousPlayMode;
	}

	/**
     * Adds an event consisting of a integer key and an integer value.
     * 
     * @param key the key.
     * @param value the value.
     */
    public void addEvent(int key, int value) {
    	ArrayList<Integer> eventSet = events.get(key);
    	if(eventSet == null) {
    		eventSet = new ArrayList<Integer>();
    		events.put(key, eventSet);
    	}
        eventSet.add(value);
    }
    
    /**
     * Removes the event with the given integer key and value.
     * 
     * @param key the key.
     */
    public void removeEvent(int key, int value) {
    	ArrayList<Integer> eventSet = events.get(key);
    	if(eventSet != null) {
    		eventSet.remove(new Integer(value));
    	}
    }
    
    /**
     * Removes the events at the given integer key (step).
     * 
     * @param key the key.
     */
    public void clearEventsAtStep(int key) {
    	events.remove(key);
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
        getEventAtStep(index);
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
    public ArrayList<Integer> getEventAtStep(int index) {
    	currentValue = null;
    	if(continuousPlayMode == ContinuousPlayMode.INTERNAL) {
	    	if(index % hop == 0) {
	    		currentValue = events.get(currentIndex);
	    		currentIndex++;
	    		if(currentIndex >= loop) reset();
	    	}
    	} else {
    		if(index % hop == 0) {
    			currentIndex = index / hop % loop;
    			currentValue = events.get(currentIndex);
    		}
    	}
        return currentValue;
    }
    
    public ArrayList<Integer> getEventAtStepQuantized(int index, int quantization) {
    	currentValue = null;
    	if(continuousPlayMode == ContinuousPlayMode.INTERNAL) {
	    	if(index % hop == 0) {
	    		currentValue = getQuantizedEvent(currentIndex, quantization);
	    		currentIndex++;
	    		if(currentIndex >= loop) reset();
	    	}
    	} else {
    		if(index % hop == 0) {
    			currentIndex = index / hop % loop;
    			currentValue = getQuantizedEvent(currentIndex, quantization);
    		}
    	}
        return currentValue;
    }
    
    private ArrayList<Integer> getQuantizedEvent(int index, int quant) {
    	if(quant == 1) return events.get(index);
    	ArrayList<Integer> collection = new ArrayList<Integer>();
    	//go from half before index to half after index
    	for(int i = - quant / 2; i < quant / 2; i++) {
    		int theRealIndex = index + i;
    		while(theRealIndex < 0) theRealIndex += loop;
    		while(theRealIndex >= loop) theRealIndex -= loop;
        	ArrayList<Integer> moreEvents = events.get(theRealIndex);	
        	if(moreEvents != null) collection.addAll(moreEvents);
    	}
    	if(collection.size() == 0) return null;
    	return collection;
    }
    
    public ArrayList<Integer> getEventAtIndex(int index) {
    	return events.get(index);
    }
    
    public int getLastIndex() {
    	if(continuousPlayMode == ContinuousPlayMode.INTERNAL) {
	    	if(currentIndex == 0) {
	    		return loop - 1;
	    	}
	    	return currentIndex - 1;
    	} else {
    		return currentIndex;
    	}
    }
    
    public Set<Integer> getEvents() {
    	return events.keySet();
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

    
}
