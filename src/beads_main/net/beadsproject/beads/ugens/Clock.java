
package net.beadsproject.beads.ugens;


import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.BeadArray;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.events.IntegerBead;

// TODO: Auto-generated Javadoc
/**
 * A sample rate clock. 
 */
public class Clock extends UGen implements IntegerBead {

    /** The interval envelope. */
    private UGen intervalEnvelope;
    
    /** The point. */
    private double point;
    
    /** The count. */
    private int count;
    
    private int ticksPerBeat;
    
    /** The listeners. */
    private BeadArray listeners;
    
    /** The click. */
    private boolean click;
    private float clickStrength;
    
    /**
	 * Instantiates a new clock.
	 * 
	 * @param context
	 *            the context
	 */
    public Clock(AudioContext context) {
        this(context, 1000.0f);
    }
    
    /**
	 * Instantiates a new clock.
	 * 
	 * @param context
	 *            the context
	 * @param interval
	 *            the interval
	 */
    public Clock(AudioContext context, float interval) {
        this(context, new Static(context, interval));
        ticksPerBeat = 16;
    }
    
    /**
	 * Instantiates a new clock.
	 * 
	 * @param context
	 *            the context
	 * @param env
	 *            the env
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
	 * @param click
	 *            the new click
	 */
	public void setClick(boolean click) {
		this.click = click;
	}

	/**
	 * Adds the message listener.
	 * 
	 * @param newListener
	 *            the new listener
	 */
	public void addMessageListener(Bead newListener) {
        listeners.add(newListener);
    }
    
    /**
	 * Removes the message listener.
	 * 
	 * @param newListener
	 *            the new listener
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
	 * @param i
	 *            the i
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
	 * @param count
	 *            the new count
	 */
    public void setCount(int count) {
        this.count = count;
    }

    /**
	 * Sets the interval envelope.
	 * 
	 * @param intervalEnvelope
	 *            the new interval envelope
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

	public int getTicksPerBeat() {
		return ticksPerBeat;
	}

	public void setTicksPerBeat(int ticksPerBeat) {
		this.ticksPerBeat = Math.max(1, ticksPerBeat);
	}
	
	public boolean isBeat() {
		return count % ticksPerBeat == 0;
	}
	
	public boolean isBeat(int mod) {
		return isBeat() && getBeatCount() % mod == 0;
	}
	
	public int getBeatCount() {
		return count / ticksPerBeat;
	}

}





