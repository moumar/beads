package net.beadsproject.beads.play;

import java.util.ArrayList;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.events.IntegerBead;

public class PatternPlayer extends Bead {

	public static enum ContinuousPlayMode {INTERNAL, EXTERNAL};
	
	private Pattern pattern;

    /** A BeadArray which is notified for each event. */
    private final BeadArray listeners;
    
    /** The integer hop. */
    private int hop;

    private ContinuousPlayMode continuousPlayMode;

    
    /** The current index. */
    private int currentIndex;
    
    /** The current value. */
    private ArrayList<Integer> currentValue;
	
	public PatternPlayer(Pattern pattern) {
		this();
		setPattern(pattern);
	}
	
	public PatternPlayer() {
		listeners = new BeadArray();
        continuousPlayMode = ContinuousPlayMode.EXTERNAL;
        setHop(1);
        reset();
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

	
	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

    public int getLastIndex() {
    	if(continuousPlayMode == ContinuousPlayMode.INTERNAL) {
	    	if(currentIndex == 0) {
	    		return pattern.getLoop() - 1;
	    	}
	    	return currentIndex - 1;
    	} else {
    		return currentIndex;
    	}
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
	    		currentValue = pattern.getEventAtIndex(currentIndex);
	    		currentIndex++;
	    		if(currentIndex >= pattern.getLoop()) reset();
	    	}
    	} else {
    		if(index % hop == 0) {
    			currentIndex = index / hop % pattern.getLoop();
//    			currentValue = events.get(currentIndex);
    			currentValue = pattern.getEventAtIndex(currentIndex);
    		}
    	}
        return currentValue;
    }
    public ArrayList<Integer> getEventAtStepQuantized(int index, int quantization) {
    	currentValue = null;
    	if(continuousPlayMode == ContinuousPlayMode.INTERNAL) {
	    	if(index % hop == 0) {
	    		currentValue = pattern.getQuantizedEvent(currentIndex, quantization);
	    		currentIndex++;
	    		if(currentIndex >= pattern.getLoop()) reset();
	    	}
    	} else {
    		if(index % hop == 0) {
    			currentIndex = index / hop % pattern.getLoop();
    			currentValue = pattern.getQuantizedEvent(currentIndex, quantization);
    		}
    	}
        return currentValue;
    }

    /**
     * Resets the pattern's current index to zero.
     */
    public void reset() {
    	currentIndex = 0;
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
