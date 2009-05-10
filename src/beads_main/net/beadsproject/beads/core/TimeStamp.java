package net.beadsproject.beads.core;

public class TimeStamp {

	public final AudioContext context;
	public final long timeStep;
	public final int index;
	
	public TimeStamp(AudioContext context, long timeStep, int index) {
		this.context = context;
		this.timeStep = timeStep;
		this.index = index;
	}
}
