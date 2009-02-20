
package net.beadsproject.beads.ugens;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;

// TODO: Auto-generated Javadoc
/**
 * The Class Recorder.
 */
public class Recorder extends UGen {

    /** The sample. */
    private Sample sample;
    
    /** The position. */
    private int position;
    
    /** The loop record. */
    private boolean loopRecord;
    
    /** The end listener. */
    private Bead endListener;
    
    /**
	 * Instantiates a new recorder.
	 * 
	 * @param player
	 *            the player
	 * @param sample
	 *            the sample
	 */
    public Recorder(AudioContext player, Sample sample) {
        super(player, sample.nChannels, 0);
        this.sample = sample;
        setLoopRecord(false);
    }
    
	/**
	 * Gets the end listener.
	 * 
	 * @return the end listener
	 */
	public Bead getEndListener() {
		return endListener;
	}

	/**
	 * Sets the end listener.
	 * 
	 * @param endListener
	 *            the new end listener
	 */
	public void setEndListener(Bead endListener) {
		this.endListener = endListener;
	}

	/**
	 * Gets the sample.
	 * 
	 * @return the sample
	 */
	public Sample getSample() {
        return sample;
    }

    /**
	 * Sets the sample.
	 * 
	 * @param sample
	 *            the new sample
	 */
    public void setSample(Sample sample) {
        this.sample = sample;
    }
    
    /**
	 * Reset.
	 */
    public void reset() {
        position = 0;
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#stop()
     */
    public void kill() {
    	super.kill();
    	if(endListener != null) endListener.message(this);
    }
    
    /**
	 * Sets the position.
	 * 
	 * @param position
	 *            the new position
	 */
    public void setPosition(float position) {
        position = sample.msToSamples(position);
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
        for(int i = 0; i < bufferSize; i++) {
            for(int j = 0; j < ins; j++) {
                sample.buf[j][position] = bufIn[j][i];
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
	 * Checks if is loop record.
	 * 
	 * @return true, if is loop record
	 */
	public boolean isLoopRecord() {
		return loopRecord;
	}

	/**
	 * Sets the loop record.
	 * 
	 * @param loopRecord
	 *            the new loop record
	 */
	public void setLoopRecord(boolean loopRecord) {
		this.loopRecord = loopRecord;
	}
	
}
