package net.beadsproject.beads.core;

public class TimeStamp {

	public final AudioContext context;
	public final long timeStep;
	public final int index;
	private double timeMs;
	private long timeSamples;
	
	public TimeStamp(AudioContext context, long timeStep, int index) {
		this.context = context;
		this.timeStep = timeStep;
		this.index = index;
	}

	public double getTimeMS() {
		timeMs = context.samplesToMs(getTimeSamples());
		return timeMs;
	}

	public long getTimeSamples() {
		timeSamples = timeStep * context.getBufferSize() + index;
		return timeSamples;
	}
}
