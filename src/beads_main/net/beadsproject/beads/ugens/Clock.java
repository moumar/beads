/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.events.IntegerBead;


// TODO: Auto-generated Javadoc
/**
 * A sample rate Clock. Clock generates timing data at two levels: ticks and beats. Clock notifies a {@link BeadArray} of listeners at each tick. These listeners can query the Clock to find out if it is on a beat, and other timing information. The rate of ticking of the Clock is controlled by an interval envelope.
 */
public class Clock extends UGen implements IntegerBead {

    /** The interval envelope. */
    private UGen intervalEnvelope;
    
    /** The current point in time of the Clock. */
    private double point;
    
    /** The current tick count of the clock. */
    private int count;

    /** The number of ticks per beat of the clock. */
    private int ticksPerBeat;
    
    /** The listeners. */
    private BeadArray listeners;
    
    /** The click. */
    private boolean click;
    
    /** The click strength. */
    private float clickStrength;
    
    /**
     * Instantiates a new clock.
     * 
     * @param context the context
     */
    public Clock(AudioContext context) {
        this(context, 1000.0f);
    }
    
    /**
     * Instantiates a new clock.
     * 
     * @param context the context
     * @param interval the interval
     */
    public Clock(AudioContext context, float interval) {
        this(context, new Static(context, interval));
        ticksPerBeat = 16;
    }
    
    /**
     * Instantiates a new clock.
     * 
     * @param context the context
     * @param env the env
     */
    public Clock(AudioContext context, UGen env) {
        super(context, 0, 0);
        intervalEnvelope = env;
        listeners = new BeadArray();
        resetImmediately();
        ticksPerBeat = 16;
        clickStrength = 0.1f;
    }
    
	/**
	 * Checks if is clicking.
	 * 
	 * @return true, if is clicking
	 */
	public boolean isClicking() {
		return click;
	}
	
	/**
	 * Sets the click.
	 * 
	 * @param click the new click
	 */
	public void setClick(boolean click) {
		this.click = click;
	}

	/**
	 * Adds the message listener.
	 * 
	 * @param newListener the new listener
	 */
	public void addMessageListener(Bead newListener) {
        listeners.add(newListener);
    }
    
    /**
     * Removes the message listener.
     * 
     * @param newListener the new listener
     */
    public void removeMessageListener(Bead newListener) {
        listeners.remove(newListener);
    }
    
    /**
     * Pause.
     */
    public void pause() {
    }
    
    /**
     * Reset immediately.
     */
    public void resetImmediately() {
        point = 0.0f;
        count = 0;
        tick();
    }
    
    /**
     * Reset on next clock.
     * 
     * @param i the i
     */
    public void resetOnNextClock(int i) {
        count = i - 1;
    }

    /**
     * Gets the count.
     * 
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count.
     * 
     * @param count the new count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Sets the interval envelope.
     * 
     * @param intervalEnvelope the new interval envelope
     */
    public void setIntervalEnvelope(UGen intervalEnvelope) {
        this.intervalEnvelope = intervalEnvelope;
    }
    
    /**
     * Gets the interval envelope.
     * 
     * @return the interval envelope
     */
    public UGen getIntervalEnvelope() {
    	return intervalEnvelope;    	
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
    	intervalEnvelope.update();
    	for(int i = 0; i < bufferSize; i++) {   
    		float value = Math.max(1.0f, intervalEnvelope.getValue(0, i) / (float)ticksPerBeat);
    		point += 1.0f / context.msToSamples(value);
    		if(point >= 1.0f) {
    			tick();
    			count++;
    			while(point >= 1.0f) point -= 1.0f;
    		}
    	}
    }
    
    /**
     * Tick.
     */
    private void tick() {
    	if(click && isBeat()) context.out.addInput(new Clicker(context, clickStrength));
    	listeners.message(this);
    }

	/* (non-Javadoc)
	 * @see com.olliebown.beads.events.IntegerBead#getInt()
	 */
	public int getInt() {
		return getCount();
	}

	/**
	 * Gets the ticks per beat.
	 * 
	 * @return the ticks per beat
	 */
	public int getTicksPerBeat() {
		return ticksPerBeat;
	}

	/**
	 * Sets the ticks per beat.
	 * 
	 * @param ticksPerBeat the new ticks per beat
	 */
	public void setTicksPerBeat(int ticksPerBeat) {
		this.ticksPerBeat = Math.max(1, ticksPerBeat);
	}
	
	/**
	 * Checks if is beat.
	 * 
	 * @return true, if is beat
	 */
	public boolean isBeat() {
		return count % ticksPerBeat == 0;
	}
	
	/**
	 * Checks if is beat.
	 * 
	 * @param mod the mod
	 * 
	 * @return true, if is beat
	 */
	public boolean isBeat(int mod) {
		return isBeat() && getBeatCount() % mod == 0;
	}
	
	/**
	 * Gets the beat count.
	 * 
	 * @return the beat count
	 */
	public int getBeatCount() {
		return count / ticksPerBeat;
	}

}





