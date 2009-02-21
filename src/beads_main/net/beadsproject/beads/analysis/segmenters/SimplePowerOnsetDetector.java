/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.analysis.segmenters;

import net.beadsproject.beads.analysis.Segmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clicker;
import net.beadsproject.beads.ugens.RTInput;

/**
 * SimplePowerOnsetDetector runs a very simple onset detection algorithm and segments audio accordingly.
 */
public class SimplePowerOnsetDetector extends Segmenter {

	//TODO I haven't implemented the recorder in this yet, so this Segmenter can't pass audio to FeatureExtractors yet.
	
    /** The number of samples to average over when calculating power. */
    static private int BLOCK = 10;
    
    /** The current beat strength. */
    private int beatStrength;
    
    /** The array of thresholds used to determine onsets at different strengths. */
    private float[] thresholds;
    
    /** The time in samples. */
    private int count;
    
    /** The hop in samples. */
    private int hop;
    
    /** The cutout time in samples. */
    private int cutout;
    
    /** The cutout count. */
    private int cutoutCount;
    
    /** The click. */
    private boolean click;
    
    /** The local count. */
    private int localCount;
    
    /**
	 * Instantiates a new SimplePowerOnsetDetector with one listener.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param listener
	 *            the listener.
	 */
    public SimplePowerOnsetDetector(AudioContext context, Bead listener) {
    	this(context);
    	addListener(listener);
    }
    
    /**
	 * Instantiates a new SimplePowerOnsetDetector.
	 * 
	 * @param context
	 *            the context
	 */
    public SimplePowerOnsetDetector(AudioContext context) {
        super(context);
        setThresholds(new float[] {0.1f, 0.2f, 0.4f});
        count = 0;
        cutout = (int) context.msToSamples(20f);
        cutoutCount = -1;
        setHop(5f);
        setClick(false);
    }
    
	/**
	 * Gets the cutout time.
	 * 
	 * @return the cutout time in milliseconds.
	 */
	public double getCutout() {
		return context.samplesToMs(cutout);
	}
	
	/**
	 * Sets the cutout time.
	 * 
	 * @param cutout
	 *            the new cutout time in milliseconds.
	 */
	public void setCutout(float cutout) {
		this.cutout = (int)context.msToSamples(cutout);
	}
    
	/**
	 * Checks if audible click is activated.
	 * 
	 * @return true if clicking.
	 */
	public boolean isClicking() {
		return click;
	}
	
	/**
	 * Sets/unsets the audible click.
	 * 
	 * @param click
	 *            true to hear click.
	 */
	public void setClick(boolean click) {
		this.click = click;
	}

	/**
	 * Gets the hop size.
	 * 
	 * @return the hop size in milliseconds.
	 */
	public double getHop() {
        return context.samplesToMs(hop);
    }
    
    /**
	 * Sets the hop size.
	 * 
	 * @param msHop
	 *            the new hop size in milliseconds.
	 */
    public void setHop(float msHop) {
        hop = (int)context.msToSamples(msHop);
        hop = hop - (hop  % BLOCK);
    }
    
	/**
	 * Gets the array of thresholds used to determine the onset layer.
	 * 
	 * @return the threshold array.
	 */
	public float[] getThresholds() {
		return thresholds;
	}

	/**
	 * Sets the array of thresholds used to determine the onset layer.
	 * 
	 * @param thresholds
	 *            the new threshold array.
	 */
	public void setThresholds(float[] thresholds) {
		this.thresholds = thresholds;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
    public void calculateBuffer() {
    	if(count > cutoutCount) {
	        localCount = 0;
	        while(localCount < bufferSize - BLOCK) {
	            //grab average of next 4 samples
	            float average = 0.0f;
	            for(int i = 0; i < BLOCK; i++) {
	                average += bufIn[0][localCount++];
	            }
	            average /= (float)BLOCK;
	            beatStrength = 0;
	            for(int i = 0; i < thresholds.length; i++) {
		            if(average > thresholds[i]) {
		            	beatStrength = i + 1;
		            }
	            }
	            if(beatStrength > 0) {
	                onset();
	                cutoutCount = count + localCount + cutout;
	                break;
	            } 
	            localCount += hop;
	        }   
    	}
        count += bufferSize;
    }
    	
    /**
	 * Gets the current beat strength, determined by the set of thresholds specified in {@link #setThresholds(float[])}.
	 * 
	 * @return the beat strength.
	 */
    public int getBeatStrength() {
    	return beatStrength;
    }
    
    /**
	 * Called whenever there's an onset.
	 */
    private void onset() {
    	if(click) context.out.addInput(new Clicker(context, 0.5f));
    	segment(null, 0);
    }
    
}
