 
package net.beadsproject.beads.ugens;


import java.util.ArrayList;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioUtils;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

// TODO: Auto-generated Javadoc
/**
 * The Class Envelope.
 */
public class Envelope extends UGen {

    /** The segments. */
    private ArrayList<Segment> segments;
    
    /** The current start value. */
    private float currentStartValue;
    
    /** The current value. */
    private float currentValue;
    
    /** The current time. */
    private int currentTime;
    
    /** The current segment. */
    private Segment currentSegment;
    
    /** The lock. */
    private boolean lock;
    
    private boolean unchanged;
    
    /**
	 * The Class Segment.
	 */
    public class Segment {
        
        /** The end value. */
        float endValue;     //no units
        
        /** The duration. */
        int duration;       //samples
        
        /** The curvature. */
        float curvature;    //no units, defines a power
        
        /** The trigger. */
        Bead trigger;
        
        /**
		 * Instantiates a new segment.
		 * 
		 * @param endValue
		 *            the end value
		 * @param duration
		 *            the duration
		 * @param curvature
		 *            the curvature
		 * @param trigger
		 *            the trigger
		 */
        public Segment(float endValue, float duration, float curvature, Bead trigger) {
            this.endValue = endValue;
            this.duration = (int)context.msToSamples(duration);
            this.curvature = Math.abs(curvature);
            this.trigger = trigger;
        }
        
    }
    
    /**
	 * Instantiates a new envelope.
	 * 
	 * @param context
	 *            the context
	 */
    public Envelope(AudioContext context) {
        super(context, 0, 1);
        segments = new ArrayList<Segment>();
        currentStartValue = 0;
        currentValue = 0;
        currentSegment = null;
        lock = false;
        unchanged = false;
    }
    
    /**
	 * Instantiates a new envelope.
	 * 
	 * @param context
	 *            the context
	 * @param value
	 *            the value
	 */
    public Envelope(AudioContext context, float value) {
    	this(context);
    	setValue(value);
    }
    
    /**
	 * Lock.
	 */
    public void lock(boolean lock) {
    	this.lock = lock;
    }
    
    public boolean isLocked() {
    	return lock;
    }
    
    /**
	 * Adds the segment.
	 * 
	 * @param endValue
	 *            the end value
	 * @param duration
	 *            the duration
	 * @param curvature
	 *            the curvature
	 * @param trigger
	 *            the trigger
	 */
    public synchronized void addSegment(float endValue, float duration, float curvature, Bead trigger) {
        if(!lock) {
        	if(!Float.isNaN(endValue) && !Float.isInfinite(endValue)) {
        		segments.add(new Segment(endValue, duration, curvature, trigger));
                unchanged = false;
        	}
        }
    }
    
    /**
	 * Adds the segment.
	 * 
	 * @param endValue
	 *            the end value
	 * @param duration
	 *            the duration
	 */
    public void addSegment(float endValue, float duration) {
    	addSegment(endValue, duration, 1.0f, null);        
    }
    
    /**
	 * Adds the segment.
	 * 
	 * @param endValue
	 *            the end value
	 * @param duration
	 *            the duration
	 * @param trigger
	 *            the trigger
	 */
    public void addSegment(float endValue, float duration, Bead trigger) {
    	addSegment(endValue, duration, 1.0f, trigger);        
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#setValue(float)
     */
    public void setValue(float value) {
    	if(!lock) {
    		clear();
        	addSegment(value, 0.0f);
    	}
    }
    
    /**
	 * Clear.
	 */
    public synchronized void clear() {
    	if(!lock) {
    		segments = new ArrayList<Segment>();
        	currentSegment = null;
    	} 
//    	else {
//    		System.out.println("warning: attempting to clear a locked envelope");
//    	}
    }
    
    /**
	 * Gets the next segment.
	 * 
	 * @return the next segment
	 */
    private synchronized void getNextSegment() {
        if(currentSegment != null) {
            currentStartValue = currentSegment.endValue;
            currentValue = currentStartValue;
            segments.remove(currentSegment);
            if(currentSegment.trigger != null) {
            	currentSegment.trigger.message(this);
            	//System.out.println("envelope:stopTrigger");
            }
        } else {
        	currentStartValue = currentValue;
        }
        if(segments.size() > 0) {
            currentSegment = segments.get(0);
        } else {
            currentSegment = null;
        }
        currentTime = 0;
    }
    
    /**
	 * Gets the current value.
	 * 
	 * @return the current value
	 */
    public float getCurrentValue() {
        return currentValue;
    }

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public synchronized void calculateBuffer() {
		if(!unchanged) {
			boolean iChanged = false;
			for(int i = 0; i < bufferSize; ++i) {
				if(currentSegment == null) {
					getNextSegment();
		        } else if(currentSegment.duration == 0) {
		            getNextSegment();
		            iChanged = true;
		        } else {
		        	iChanged = true;
		        	float ratio;
		        	if(currentSegment.curvature != 1.0f) ratio = (float)AudioUtils.fastPow((double)currentTime / (double)currentSegment.duration, (double)currentSegment.curvature);
		        	else ratio = (float)currentTime / (float)currentSegment.duration;
		            currentValue = (1f - ratio) * currentStartValue + ratio * currentSegment.endValue;
		            currentTime++;
		            if(currentTime > currentSegment.duration) getNextSegment();
		        }
				bufOut[0][i] = currentValue;
			}
			if(!iChanged) unchanged = true;
		}
	}
    
}
