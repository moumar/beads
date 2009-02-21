/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Gain modifies the gain of a multi-channel audio signal. The gain value can be controlled by an audio signal.
 *
 * @author ollie
 */
public class Gain extends UGen {

    /** The gain envelope. */
    private UGen gainEnvelope;
    
    /**
	 * Instantiates a new Gain.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param inouts
	 *            the number of inputs (= number of outputs).
	 * @param gainEnvelope
	 *            the gain envelope.
	 */
    public Gain(AudioContext context, int inouts, UGen gainEnvelope) {
       this(context, inouts);
        setGainEnvelope(gainEnvelope);
    }
    
    /**
	 * Instantiates a new Gain with {@link Static} gain envelop set to 1.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param inouts
	 *            the number of inputs (= number of outputs).
	 */
    public Gain(AudioContext context, int inouts) {
        super(context, inouts, inouts);
        gainEnvelope = new Static(context, 1.0f);
    }
    
    /**
	 * Gets the gain envelope.
	 * 
	 * @return the gain envelope.
	 */
    public UGen getGainEnvelope() {
        return gainEnvelope;
    }
    
    /**
	 * Sets the gain envelope.
	 * 
	 * @param gainEnvelope
	 *            the new gain envelope.
	 */
    public void setGainEnvelope(UGen gainEnvelope) {
        this.gainEnvelope = gainEnvelope;
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
    	gainEnvelope.update();
        for(int i = 0; i < bufferSize; ++i) {
            float gain = gainEnvelope.getValue(0, i);
            for(int j = 0; j < ins; ++j) {
                bufOut[j][i] = gain * bufIn[j][i];
            }
        }
    }

}
