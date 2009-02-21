/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;

/**
 * Recorder records audio into a {@link Sample}. If a Recorder is not in loop mode it kills itself when it reaches the end of the {@link Sample}.
 */
public class Recorder extends UGen {

    /** The Sample to record into. */
    private Sample sample;
    
    /** The position in samples. */
    private long position;
    
    /** Flag to determine if looping is on. */
    private boolean loopRecord;
    
    /**
	 * Instantiates a new Recorder.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param sample
	 *            the Sample.
	 */
    public Recorder(AudioContext context, Sample sample) {
        super(context, sample.nChannels, 0);
        this.sample = sample;
        setLoopRecord(false);
    }

	/**
	 * Gets the Sample.
	 * 
	 * @return the Sample.
	 */
	public Sample getSample() {
        return sample;
    }

    /**
	 * Sets the Sample.
	 * 
	 * @param sample
	 *            the new Sample.
	 */
    public void setSample(Sample sample) {
        this.sample = sample;
    }
    
    /**
	 * Resets the Recorder to record into the beginning of the Sample.
	 */
    public void reset() {
        position = 0;
    }
    
    /**
	 * Sets the position to record to in milliseconds.
	 * 
	 * @param position
	 *            the new position in milliseconds.
	 */
    public void setPosition(double position) {
        position = sample.msToSamples(position);
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
        for(int i = 0; i < bufferSize; i++) {
            for(int j = 0; j < ins; j++) {
                sample.buf[j][(int)position] = bufIn[j][i];
            }
            position++;
            if(position >= sample.buf[0].length) {
            	if(loopRecord) position = 0;
            	else {
            		kill();
            		break;
            	}
            }
        }
    }
    
    /**
	 * Gets the position.
	 * 
	 * @return the position
	 */
    public double getPosition() {
    	return context.samplesToMs(position);
    }

	/**
	 * Checks if loop record mode is enabled.
	 * 
	 * @return true if loop record mode is enabled.
	 */
	public boolean isLoopRecord() {
		return loopRecord;
	}

	/**
	 * Starts/stops loop record mode.
	 * 
	 * @param loopRecord true to enable loop record mode.
	 */
	public void setLoopRecord(boolean loopRecord) {
		this.loopRecord = loopRecord;
	}
	
}
