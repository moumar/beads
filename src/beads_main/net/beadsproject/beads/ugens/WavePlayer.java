
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.buffers.SawBuffer;
import net.beadsproject.beads.data.buffers.SineBuffer;

// TODO: Auto-generated Javadoc
//This should be rebuilt without the input, which is superfluous now that 
//frequencyEnvelope is a UGen -- i.e., frequency modulation can be done via that alone.

/**
 * The Class WavePlayer.
 */
public class WavePlayer extends UGen {

    /** The point. */
    double point;
    
    /** The frequency envelope. */
    UGen frequencyEnvelope;
    
    /** The buffer. */
    Buffer buffer;
    
    /**
	 * Instantiates a new wave player.
	 * 
	 * @param context
	 *            the context
	 * @param frequencyEnvelope
	 *            the frequency envelope
	 * @param buffer
	 *            the buffer
	 */
    public WavePlayer(AudioContext context, UGen frequencyEnvelope, Buffer buffer) {
        super(context, 1);
        this.frequencyEnvelope = frequencyEnvelope;
        this.buffer = buffer;
    }
    
    /**
	 * Instantiates a new wave player.
	 * 
	 * @param context
	 *            the context
	 * @param frequency
	 *            the frequency
	 * @param buffer
	 *            the buffer
	 */
    public WavePlayer(AudioContext context, float frequency, Buffer buffer) {
        super(context, 1, 1);
        frequencyEnvelope = new Envelope(context);
        ((Envelope)frequencyEnvelope).setValue(frequency);
        this.buffer = buffer;
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#start()
     */
    public void start() {
        super.start();
        point = 0;
    }
    
    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void calculateBuffer() {
    	frequencyEnvelope.update();
        float sr = context.getSampleRate();
        for(int i = 0; i < bufferSize; i++) {
            float frequency = Math.abs(frequencyEnvelope.getValue(0, i));
            point += (frequency / sr);
            point = point % 1.0f;
            bufOut[0][i] = buffer.getValueFraction((float)point);
         }
    }

	/**
	 * Gets the frequency envelope.
	 * 
	 * @return the frequency envelope
	 */
	public UGen getFrequencyEnvelope() {
		return frequencyEnvelope;
	}

	/**
	 * Sets the frequency envelope.
	 * 
	 * @param frequencyEnvelope
	 *            the new frequency envelope
	 */
	public void setFrequencyEnvelope(UGen frequencyEnvelope) {
		this.frequencyEnvelope = frequencyEnvelope;
	}
	
	public void setBuffer(Buffer b) {
		this.buffer = b;
	}
	
}
