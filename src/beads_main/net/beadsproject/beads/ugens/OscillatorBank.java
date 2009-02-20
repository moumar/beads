/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.SineBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class OscillatorBank.
 */
public class OscillatorBank extends UGen {

    /** The frequency. */
    float[] frequency;
    
    /** The gains. */
    float[] gains;
    
    /** The point. */
    float[] point;
    
    /** The increment. */
    float[] increment;
    
    /** The buffer. */
    Buffer buffer;
    
    /** The num oscillators. */
    int numOscillators;
    
    /** The gain. */
    float sr, gain;
    
    /**
	 * Instantiates a new oscillator bank.
	 * 
	 * @param player
	 *            the player
	 * @param buffer
	 *            the buffer
	 * @param numOscillators
	 *            the num oscillators
	 */
    public OscillatorBank(AudioContext player, Buffer buffer, int numOscillators) {
        super(player, 1, 1);
        this.buffer = buffer;
        sr = context.getSampleRate();
        setNumOscillators(numOscillators);
        gain = 1f / (float)numOscillators;
    }
    
    public void setNumOscillators(int numOscillators) {
    	this.numOscillators = numOscillators;
		float[] old = frequency;
		frequency = new float[numOscillators];
		increment = new float[numOscillators];
		int min = 0;
		if(old != null) min = Math.min(frequency.length, old.length);
		for(int i = 0; i < min; i++) {
			frequency[i] = old[i];
            increment[i] = frequency[i] / context.getSampleRate();
		}
		for(int i = min; i < frequency.length; i++) {
			frequency[i] = 0f;
            increment[i] = frequency[i] / context.getSampleRate();
		}
		old = gains;
		gains = new float[numOscillators];
		for(int i = 0; i < min; i++) {
			gains[i] = old[i];
		}
		for(int i = min; i < gains.length; i++) {
			gains[i] = 1f;
		}    		
		old = point;
		point = new float[numOscillators];
		for(int i = 0; i < min; i++) {
			point[i] = old[i];
		}
		for(int i = min; i < point.length; i++) {
			point[i] = 0f;
		}
    }
    
    /**
	 * Sets the frequencies.
	 * 
	 * @param frequencies
	 *            the new frequencies
	 */
    public void setFrequencies(float[] frequencies) {
        for(int i = 0; i < numOscillators; i++) {
        	if(i < frequencies.length) {
        		frequency[i] = Math.abs(frequencies[i]);
        	} else {
        		frequency[i] = 0f;
        	}
            increment[i] = frequency[i] / context.getSampleRate();
        }
    }
    
    /**
	 * Sets the gains.
	 * 
	 * @param gains
	 *            the new gains
	 */
    public void setGains(float[] gains) {
    	for(int i = 0; i < numOscillators; i++) {
        	if(i < gains.length) {
        		this.gains[i] = gains[i];
        	} else {
        		this.gains[i] = 0f;
        	}
        }
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
        zeroOuts();
        for(int i = 0; i < bufferSize; i++) {
            for(int j = 0; j < numOscillators; j++) {
                point[j] = (point[j] + increment[j]) % 1f;
                bufOut[0][i] += gains[j] * buffer.getValueFraction(point[j]);
            }
            bufOut[0][i] *= gain;
        }
    }   

    
}




