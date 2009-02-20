package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

// TODO: Auto-generated Javadoc
/**
 * The Class DelayTrigger.
 */
public class DelayTrigger extends UGen {

	/** The sample delay. */
	private long sampleDelay;
	
	/** The count. */
	private long count;
	
	/** The receiver. */
	private Bead receiver;
	
	/**
	 * Instantiates a new delay trigger.
	 * 
	 * @param context
	 *            the context
	 * @param delay
	 *            the delay
	 * @param receiver
	 *            the receiver
	 */
	public DelayTrigger(AudioContext context, float delay, Bead receiver) {
		super(context, 0, 0);
		sampleDelay = (long)context.msToSamples(delay);
		reset();
		this.receiver = receiver;
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		count = 0;
	}

	/* (non-Javadoc)
	 * @see com.olliebown.beads.core.UGen#calculateBuffer()
	 */
	@Override
	public void calculateBuffer() {
		if(sampleDelay - count > bufferSize) {
			count += bufferSize;
		} else {
			if(receiver != null) {
				receiver.message(this);
			}
			kill();
		}
	}

	/**
	 * Gets the sample delay.
	 * 
	 * @return the sample delay
	 */
	public double getSampleDelay() {
		return context.samplesToMs(sampleDelay);
	}

	/**
	 * Sets the sample delay.
	 * 
	 * @param sampleDelay
	 *            the new sample delay
	 */
	public void setSampleDelay(float sampleDelay) {
		this.sampleDelay = (long)context.msToSamples(sampleDelay);
	}

	/**
	 * Gets the receiver.
	 * 
	 * @return the receiver
	 */
	public Bead getReceiver() {
		return receiver;
	}

	/**
	 * Sets the receiver.
	 * 
	 * @param receiver
	 *            the new receiver
	 */
	public void setReceiver(Bead receiver) {
		this.receiver = receiver;
	}

	/**
	 * Gets the count.
	 * 
	 * @return the count
	 */
	public double getCount() {
		return context.samplesToMs(count);
	}
	
}
