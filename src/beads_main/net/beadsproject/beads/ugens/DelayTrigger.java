/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

/**
 * A DelayTrigger waits for a specified duration and then notifies a receiver.
 *
 * @author ollie
 */
public class DelayTrigger extends UGen {

	/** The duration of the delay in samples. */
	private long sampleDelay;
	
	/** The current count in samples. */
	private long count;
	
	/** The Bead that responds to is DelayTrigger. */
	private Bead receiver;
	
	/**
	 * Instantiates a new DelayTrigger with the specified millisecond delay and receiver.
	 * 
	 * @param context
	 *            the AudioContext.
	 * @param delay
	 *            the delay in milliseconds.
	 * @param receiver
	 *            the receiver.
	 */
	public DelayTrigger(AudioContext context, double delay, Bead receiver) {
		super(context, 0, 0);
		sampleDelay = (long)context.msToSamples(delay);
		reset();
		this.receiver = receiver;
	}
	
	/**
	 * Reset timer to zero.
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
	 * @return the sample delay in milliseconds.
	 */
	public double getSampleDelay() {
		return context.samplesToMs(sampleDelay);
	}

	/**
	 * Sets the sample delay. This may cause the DelayTrigger to trigger immediately.
	 * 
	 * @param sampleDelay
	 *            the new sample delay in milliseconds.
	 */
	public void setSampleDelay(float sampleDelay) {
		this.sampleDelay = (long)context.msToSamples(sampleDelay);
	}

	/**
	 * Gets this DelayTrigger's receiver.
	 * 
	 * @return the receiver.
	 */
	public Bead getReceiver() {
		return receiver;
	}

	/**
	 * Sets this DelayTrigger's receiver.
	 * 
	 * @param receiver
	 *            the new receiver.
	 */
	public void setReceiver(Bead receiver) {
		this.receiver = receiver;
	}

	/**
	 * Gets the current count.
	 * 
	 * @return the count in milliseconds.
	 */
	public double getCount() {
		return context.samplesToMs(count);
	}
	
}
