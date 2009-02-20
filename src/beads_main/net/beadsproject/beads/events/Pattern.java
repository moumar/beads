
package net.beadsproject.beads.events;


import java.util.Hashtable;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;

// TODO: Auto-generated Javadoc
/**
 * The Class Pattern.
 */
public class Pattern extends Bead implements IntegerBead {
	
    /** The events. */
    private final Hashtable<Integer, Integer> events;
    
    /** The listener. */
    private final BeadArray listeners;
    
    /** The mult. */
    private int hop;
    
    /** The mod. */
    private int loop;
    
    private int currentIndex;
    
    /** The current value. */
    private Integer currentValue;
    
    /**
	 * Instantiates a new pattern.
	 */
    public Pattern() {
    	listeners = new BeadArray();
        events = new Hashtable<Integer, Integer>();
        setNoLoop();
        setHop(1);
        reset();
    }
    
    public void reset() {
    	currentIndex = 0;
    }
    
    /**
	 * Adds the listener.
	 * 
	 * @param listener
	 *            the new listener
	 */
    public void addListener(Bead listener) {
    	listeners.add(listener);
    }
    
    public void removeListener(Bead listener) {
    	listeners.remove(listener);
    }
    
    /**
	 * Adds the event.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
    public void addEvent(int key, int value) {
        events.put(key, value);
    }
    
    /**
	 * Removes the event.
	 * 
	 * @param key
	 *            the key
	 */
    public void removeEvent(int key) {
    	for(int index : events.keySet()) {
            if(index == key) {
                events.remove(index);
                break;
            }
        }
    }
    
    public void clear() {
    	events.clear();
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.Bead#message(com.olliebown.beads.core.Bead)
     */
    public void messageReceived(Bead message) {
        int time = ((IntegerBead)message).getInt();
        getEventAtTime(time);
        if(currentValue != null) {
    		listeners.message(this);
    	}
    }
    
    public Integer getEventAtTime(int time) {
    	currentValue = null;
    	if(time % hop == 0) {
    		currentValue = events.get(currentIndex);
    		currentIndex++;
    		if(currentIndex >= loop) reset();
    	}
    	
//        if(loop != Integer.MAX_VALUE) time = time % (loop * hop);            
//        for(int index : events.keySet()) {
//            if(index * hop == time) {
//            	currentValue = events.get(index);
//            }
//        }
    	
        return currentValue;
    }

    /**
	 * Gets the mod.
	 * 
	 * @return the mod
	 */
    public int getLoop() {
        return loop;
    }

    /**
	 * Sets the mod.
	 * 
	 * @param mod
	 *            the new mod
	 */
    public void setLoop(int loop) {
        this.loop = loop;
    }
    
    public void setNoLoop() {
    	loop = Integer.MAX_VALUE;
    }

	/**
	 * Gets the mult.
	 * 
	 * @return the mult
	 */
	public int getHop() {
		return hop;
	}

	/**
	 * Sets the mult.
	 * 
	 * @param mult
	 *            the new mult
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
	
	public int getValue() {
		return currentValue;
	}
    
}
