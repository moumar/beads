package net.beadsproject.beads.core;

/**
 * TimeStamps store time with respect to the current {@link AudioContext}. Specificially, the TimeStamp stores a time step and an index into a buffer. 
 */
public class TimeStamp {

	/** The context. */
	public final AudioContext context;
	
	/** The time step (AudioContext's count of sample frames). */
	public final long timeStep;
	
	/** The index into the sample frame. */
	public final int index;
	
	/** The time ms. */
	private double timeMs;
	
	/** The time samples. */
	private long timeSamples;
	
	/**
	 * Instantiates a new TimeStamp with the given time step, context and buffer index. Use {@link AudioContext.generateTimeStamp()} to generate a
	 * TimeStamp for the current time.
	 * 
	 * @param context the AudioContext.
	 * @param timeStep the time step.
	 * @param index the index.
	 */
	public TimeStamp(AudioContext context, long timeStep, int index) {
		this.context = context;
		this.timeStep = timeStep;
		this.index = index;
	}

	/**
	 * Gets the time of the TimeStamp in milliseconds.
	 * 
	 * @return the time in milliseconds.
	 */
	public double getTimeMS() {
		timeMs = context.samplesToMs(getTimeSamples());
		return timeMs;
	}

	/**
	 * Gets the time in samples.
	 * 
	 * @return the time in samples.
	 */
	public long getTimeSamples() {
		timeSamples = timeStep * context.getBufferSize() + index;
		return timeSamples;
	}
}
